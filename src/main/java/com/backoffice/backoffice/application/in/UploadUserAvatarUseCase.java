package com.backoffice.backoffice.application.in;

import java.io.InputStream;
import java.util.UUID;

public interface UploadUserAvatarUseCase {
    String execute(UUID userId, InputStream file, long size);
}