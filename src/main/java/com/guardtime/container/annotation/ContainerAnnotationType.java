package com.guardtime.container.annotation;

public enum ContainerAnnotationType {

    FULLY_REMOVABLE("ksie10/removable-fully"),
    VALUE_REMOVABLE("ksie10/removable-value"),
    NON_REMOVABLE("ksie10/removable-none");

    private String content;

    ContainerAnnotationType(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
