package com.mmi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Extensions autorisées par type
    private static final Set<String> ALLOWED_THUMBNAILS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_FILES      = Set.of("jpg", "jpeg", "png", "webp", "pdf", "mp4", "glb", "gltf");

    // Tailles max
    private static final long MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024;   // 5 MB
    private static final long MAX_FILE_SIZE      = 50 * 1024 * 1024;  // 50 MB

    // Magic bytes (premiers octets) pour valider le vrai type
    private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>() {{
        put("jpg",  new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});
        put("jpeg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});
        put("png",  new byte[]{(byte)0x89, 0x50, 0x4E, 0x47});
        put("pdf",  new byte[]{0x25, 0x50, 0x44, 0x46});
        put("webp", new byte[]{0x52, 0x49, 0x46, 0x46}); // RIFF
    }};

    /**
     * Stocke un fichier et retourne son URL publique.
     * @param file     le fichier uploadé
     * @param subDir   "thumbnails" ou "fichiers"
     * @param isThumbnail true = validation thumbnail, false = validation fichier projet
     */
    public String store(MultipartFile file, String subDir, boolean isThumbnail) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        // 1. Extension
        String originalName = Objects.requireNonNull(file.getOriginalFilename(), "Nom de fichier manquant");
        String ext = getExtension(originalName).toLowerCase();

        Set<String> allowed = isThumbnail ? ALLOWED_THUMBNAILS : ALLOWED_FILES;
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("Extension non autorisée : " + ext + ". Autorisées : " + allowed);
        }

        // 2. Taille
        long maxSize = isThumbnail ? MAX_THUMBNAIL_SIZE : MAX_FILE_SIZE;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Fichier trop lourd. Maximum : " + (maxSize / 1024 / 1024) + " MB");
        }

        // 3. Magic bytes (seulement pour les types connus)
        if (MAGIC_BYTES.containsKey(ext)) {
            byte[] header = Arrays.copyOf(file.getBytes(), 8);
            byte[] expected = MAGIC_BYTES.get(ext);
            for (int i = 0; i < expected.length; i++) {
                if (header[i] != expected[i]) {
                    throw new IllegalArgumentException("Le contenu du fichier ne correspond pas à l'extension déclarée");
                }
            }
        }

        // 4. Stockage
        Path dir = Paths.get(uploadDir, subDir);
        Files.createDirectories(dir);

        String uniqueName = UUID.randomUUID() + "." + ext;
        Path dest = dir.resolve(uniqueName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + subDir + "/" + uniqueName;
    }

    /**
     * Supprime un fichier à partir de son URL publique.
     */
    public void delete(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;
        try {
            // /uploads/thumbnails/xxx.jpg → uploads/thumbnails/xxx.jpg
            String relative = publicUrl.replaceFirst("^/", "");
            Path file = Paths.get(relative);
            Files.deleteIfExists(file);
        } catch (IOException ignored) {}
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) throw new IllegalArgumentException("Fichier sans extension");
        return filename.substring(dot + 1);
    }

    public String storeAvatar(MultipartFile file) throws IOException {
        String ext      = getExtension(file.getOriginalFilename());
        String filename = "avatar_" + UUID.randomUUID() + "." + ext;
        Path   target   = Paths.get(uploadDir, "avatars", filename);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/avatars/" + filename;
    }

}