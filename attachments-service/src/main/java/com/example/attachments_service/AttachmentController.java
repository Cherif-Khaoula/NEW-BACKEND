package com.example.attachments_service;


import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import java.util.List;



@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {





        private final AttachmentService attachmentService;

        public AttachmentController(AttachmentService attachmentService) {
            this.attachmentService = attachmentService;
        }

        @PostMapping("/upload")
        public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
            try {
                Attachment savedAttachment = attachmentService.uploadFile(file);
                return ResponseEntity.ok(savedAttachment);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Erreur lors du téléchargement : " + e.getMessage());
            }
        }

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long id) throws IOException {
        Attachment attachment = attachmentService.getFile(id);
        Path filePath = Paths.get(attachment.getFilePath()).toAbsolutePath().normalize();

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("Le fichier demandé n'existe pas !");
        }

        // Détection du type MIME
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream"; // Type par défaut pour éviter les erreurs
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"") // ⬅️ Forcer le téléchargement
                .body(resource);
    }



    @GetMapping("/view-by-path")
    public ResponseEntity<Resource> viewFileByPath(@RequestParam String filePath) throws IOException {
        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("Le fichier demandé n'existe pas !");
        }

        // Détection du type MIME
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }


    @GetMapping
    public ResponseEntity<List<Attachment>> getAllFiles() {
        return ResponseEntity.ok(attachmentService.getAllFiles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        attachmentService.deleteFile(id);
        return ResponseEntity.ok("Fichier supprimé avec succès.");
    }
}
