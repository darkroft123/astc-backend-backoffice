package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

@Input("DeleteUserInput")
public class DeleteUserInput {
    public @NonNull String id;
}
