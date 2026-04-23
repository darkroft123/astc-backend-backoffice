package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Input;

@Input("ListUsersFilterInput")
public class ListUsersFilterInput {
    public String keyword;
    public String role;
    public String status;
    public String projectId;
    public Integer page;
    public Integer size;
}
