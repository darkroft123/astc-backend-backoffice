package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Type;
import java.util.List;

@Type("ListUsersOutput")
public class ListUsersOutput {
    public List<UserOutput> items;
    public PageInfoOutput pageInfo;
}
