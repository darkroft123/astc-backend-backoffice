package com.backoffice.backoffice.infrastructure.controller.rest;

import com.backoffice.backoffice.application.in.UploadUserAvatarUseCase;
import com.backoffice.backoffice.infrastructure.controller.rest.dto.FileUploadForm;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/rest/files")
public class FileUploadResource {

    @Inject
    UploadUserAvatarUseCase uploadUseCase;

    @POST
    @Path("/avatar/{userId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("userId") UUID userId,
                           FileUploadForm form) {

        String url = uploadUseCase.execute(
                userId,
                form.file,
                form.fileSize
        );

        return Response.ok(url).build();
    }
}