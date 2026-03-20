package com.mmi.controller;

import com.mmi.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * POST /api/upload/thumbnail
     * Champ multipart : "file"
     * Extensions autorisées : jpg, jpeg, png, webp (max 5MB)
     * Retourne : { "url": "/uploads/thumbnails/uuid.jpg" }
     */
    @PostMapping("/thumbnail")
    public ResponseEntity<?> uploadThumbnail(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.store(file, "thumbnails", true);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur serveur lors de l'upload"));
        }
    }

    /**
     * POST /api/upload/fichier
     * Champ multipart : "file"
     * Extensions autorisées : jpg, jpeg, png, webp, pdf, mp4, glb, gltf (max 50MB)
     * Retourne : { "url": "/uploads/fichiers/uuid.pdf" }
     */
    @PostMapping("/fichier")
    public ResponseEntity<?> uploadFichier(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.store(file, "fichiers", false);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur serveur lors de l'upload"));
        }
    }
}