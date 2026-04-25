package com.backoffice.backoffice.infrastructure.controller.rest.dto;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;

public class FileUploadForm {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream file;

    @FormParam("fileSize")
    public long fileSize;
}