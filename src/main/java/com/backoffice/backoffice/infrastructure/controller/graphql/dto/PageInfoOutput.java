package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Type;

@Type("PageInfoOutput")
public class PageInfoOutput {
    public int page;
    public int size;
    public long totalCount;
    public int totalPages;
    public boolean hasNextPage;
}
