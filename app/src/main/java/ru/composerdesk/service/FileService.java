package ru.composerdesk.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.exception.BadRequestException;
import ru.composerdesk.exception.FileUploadException;

import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final MinioClient minioClient;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "mp3", "wav", "ogg", "txt", "pdf", "md"
    );

    @Value("${minio.bucket}")
    private String bucket;

    public FileService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = folder + "/" + UUID.randomUUID() + "_" + safeFilename;

        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot > 0) {
            ext = originalFilename.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            log.warn("Blocked file upload: {}", ext);
            throw new BadRequestException("File type not allowed: " + ext);
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("File uploaded: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Failed to upload file: {}", safeFilename, e);
            throw new FileUploadException("Failed to upload file: " + safeFilename);
        }
    }

    public void deleteFile(String s3Key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(s3Key)
                            .build());
            log.info("File deleted: {}", s3Key);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", s3Key, e);
            throw new FileUploadException("Failed to delete file: " + s3Key);
        }
    }
}