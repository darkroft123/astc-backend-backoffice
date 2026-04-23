package com.backoffice.backoffice.infrastructure.sender;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class S3Service {

    @Inject
    S3Client s3;

    private static final String BUCKET = "document";

    // 🔥 AVATAR ÚNICO (SE SOBREESCRIBE)
    public String uploadUserAvatar(UUID userId, InputStream inputStream, long size) {

        String key = "private/user/" + userId + "/photo/avatar.jpg";

        uploadFile(key, inputStream, size, "image/jpeg");

        return buildUrl(key);
    }

    // 🔥 JUSTIFICANTES (MULTIPLES)
    public String uploadJustification(UUID userId, InputStream inputStream, long size) {

        String fileId = UUID.randomUUID().toString();

        String key = "private/user/" + userId + "/justifications/" + fileId + ".pdf";

        uploadFile(key, inputStream, size, "application/pdf");

        return buildUrl(key);
    }

    // 🔧 MÉTODO BASE
    private void uploadFile(String key, InputStream inputStream, long size, String contentType) {

        if (inputStream == null) {
            throw new RuntimeException("File is null");
        }

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(key)
                        .contentLength(size)
                        .contentType(contentType)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, size));
    }

    // 🔧 URL FINAL
    private String buildUrl(String key) {
        return "http://localhost:9000/" + BUCKET + "/" + key;
    }

    @Inject
    S3Presigner presigner;

    public String generatePresignedUrl(String key) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // 🔥 expira en 5 min
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}