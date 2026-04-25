package com.backoffice.backoffice.infrastructure.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.InputStream;

@ApplicationScoped
public class MinioStorageAdapter {

    @Inject
    S3Client s3Client;

    private final String bucket = "document";

    public String upload(String path, InputStream file, long size) {

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build(),
            RequestBody.fromInputStream(file, size)
        );

        return path;
    }
}