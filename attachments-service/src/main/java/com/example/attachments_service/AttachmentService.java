package com.example.attachments_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AttachmentService {

    private final String UPLOAD_DIR = "C:/uploads/";

    private final AttachmentRepository attachmentRepository;

    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment uploadFile(MultipartFile file) throws IOException {
        // Vérifier si le dossier d'upload existe, sinon le créer

// Vérifier si le dossier d'upload existe, sinon le créer
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("Impossible de créer le dossier d'upload !");
        }

        // Sauvegarde du fichier sur disque
        String filePath = UPLOAD_DIR + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        // Sauvegarde des informations du fichier dans la BDD
        Attachment attachment = new Attachment(
                file.getOriginalFilename(),
                filePath,
                file.getContentType(),
                file.getSize()
        );

        return attachmentRepository.save(attachment);
    }

    public Attachment getFile(Long id) {
        return attachmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Fichier introuvable"));
    }

    public List<Attachment> getAllFiles() {
        return attachmentRepository.findAll();
    }

    public void deleteFile(Long id) {
        Attachment attachment = getFile(id);
        File file = new File(attachment.getFilePath());

        if (file.exists()) {
            file.delete();
        }

        attachmentRepository.deleteById(id);
    }
}
