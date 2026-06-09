package com.backoffice.backoffice.infrastructure.storage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@ApplicationScoped
public class S3StorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(S3StorageAdapter.class);

    @ConfigProperty(name = "app.storage.endpoint")
    String endpoint;

    @ConfigProperty(name = "app.storage.access-key")
    String accessKey;

    @ConfigProperty(name = "app.storage.secret-key")
    String secretKey;

    @ConfigProperty(name = "app.storage.region")
    String region;

    @ConfigProperty(name = "app.storage.bucket")
    String bucket;

    private S3Client s3Client;

    @PostConstruct
    void init() {
        log.info("Initializing S3 client... endpoint={}, region={}, bucket={}", endpoint, region, bucket);

        try {
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .forcePathStyle(true)
                    .build();

            log.info("S3 client initialized successfully");

            ensureBucketExists();

        } catch (Exception e) {
            log.error("Error initializing S3 client", e);
            throw e;
        }
    }

    private void ensureBucketExists() {
        try {
            log.info("Checking if bucket '{}' exists...", bucket);

            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());

            log.info("Bucket '{}' already exists", bucket);

        } catch (NoSuchBucketException e) {
            log.warn("Bucket '{}' not found. Creating...", bucket);

            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());

            log.info("Bucket '{}' created successfully", bucket);
        } catch (Exception e) {
            log.error("Error checking/creating bucket '{}'", bucket, e);
            throw e;
        }
    }

    public String upload(String folder, String originalFilename, InputStream data, long size, String contentType) {
        String key = folder + "/" + UUID.randomUUID() + "_" + originalFilename;

        log.info("Uploading file to S3 -> bucket={}, key={}, size={}, contentType={}",
                bucket, key, size, contentType);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(data, size));

            log.info("File uploaded successfully -> key={}", key);

            return key;

        } catch (Exception e) {
            log.error("Error uploading file to S3 -> key={}", key, e);
            throw e;
        }
    }

    public String getPublicUrl(String key) {
        String url = endpoint + "/" + bucket + "/" + key;

        log.debug("Generated public URL -> {}", url);

        return url;
    }
}