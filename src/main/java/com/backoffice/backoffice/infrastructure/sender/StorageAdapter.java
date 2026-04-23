package com.backoffice.backoffice.infrastructure.sender;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Adapter for S3/MinIO file storage.
 * Ready for future file upload features.
 */
@ApplicationScoped
public class StorageAdapter {

    private static final Logger LOG = Logger.getLogger(StorageAdapter.class);

    @Inject
    S3Client s3Client;

    public String uploadFile(String bucket, String key, byte[] data, String contentType) {
        // TODO: implement when file uploads are needed
        LOG.infof("StorageAdapter.uploadFile called for bucket=%s key=%s (not yet implemented)", bucket, key);
        throw new UnsupportedOperationException("File upload not yet implemented");
    }

    public byte[] downloadFile(String bucket, String key) {
        LOG.infof("StorageAdapter.downloadFile called for bucket=%s key=%s (not yet implemented)", bucket, key);
        throw new UnsupportedOperationException("File download not yet implemented");
    }
}
