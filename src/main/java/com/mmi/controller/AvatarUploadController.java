package com.mmi.controller;

import com.mmi.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class AvatarUploadController {

    private final FileStorageService fileStorageService;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg","jpeg","png","webp","gif");
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2 Mo

    /**
     * POST /api/upload/avatar
     * Upload d'une photo de profil personnalisée
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // Validation taille
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier trop lourd (max 2 Mo)"));
        }

        // Validation extension
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Format non supporté. Utilisez : " + String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        // Validation magic bytes (JPG: FF D8, PNG: 89 50, GIF: 47 49, WEBP: 52 49)
        try {
            byte[] header = file.getBytes();
            if (!isValidImageHeader(header, ext)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Fichier invalide ou corrompu"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Impossible de lire le fichier"));
        }

        try {
            String url = fileStorageService.storeAvatar(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors du stockage"));
        }
    }

    private boolean isValidImageHeader(byte[] bytes, String ext) {
        if (bytes.length < 4) return false;
        return switch (ext) {
            case "jpg", "jpeg" -> bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8;
            case "png"         -> bytes[0] == (byte)0x89 && bytes[1] == (byte)0x50;
            case "gif"         -> bytes[0] == (byte)0x47 && bytes[1] == (byte)0x49;
            case "webp"        -> bytes[0] == (byte)0x52 && bytes[1] == (byte)0x49;
            default            -> false;
        };
    }
}