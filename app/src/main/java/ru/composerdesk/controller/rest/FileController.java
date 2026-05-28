package ru.composerdesk.controller.rest;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Файлы", description = "Отдача файлов из хранилища")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public FileController(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @GetMapping("/**")
    @Operation(summary = "Получить файл по ключу")
    public ResponseEntity<byte[]> getFile(HttpServletRequest request) {
        String path = request.getRequestURI();
        String s3Key = path.substring("/api/files/".length());

        if (s3Key == null || s3Key.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(s3Key)
                        .build())) {

            byte[] bytes = stream.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            if (s3Key.endsWith(".jpg") || s3Key.endsWith(".jpeg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (s3Key.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (s3Key.endsWith(".gif")) {
                headers.setContentType(MediaType.IMAGE_GIF);
            } else if (s3Key.endsWith(".mp3")) {
                headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            } else if (s3Key.endsWith(".wav")) {
                headers.setContentType(MediaType.parseMediaType("audio/wav"));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            return ResponseEntity.ok().headers(headers).body(bytes);

        } catch (Exception e) {
            if (e.getCause() instanceof java.io.IOException
                    && e.getCause().getMessage() != null
                    && e.getCause().getMessage().contains("разорвала")) {
                return null;
            }
            log.error("File error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}