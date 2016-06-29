package com.guardtime.container.annotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Annotation types define annotation persistence.
 */
public enum ContainerAnnotationType {

    FULLY_REMOVABLE("ksie10/removable-fully"),
    VALUE_REMOVABLE("ksie10/removable-value"),
    NON_REMOVABLE("ksie10/removable-none");

    private String content;
    private static Map<String, ContainerAnnotationType> types;

    static {
        types = new HashMap<>();
        types.put(FULLY_REMOVABLE.getContent(), FULLY_REMOVABLE);
        types.put(VALUE_REMOVABLE.getContent(), VALUE_REMOVABLE);
        types.put(NON_REMOVABLE.getContent(), NON_REMOVABLE);
    }

    ContainerAnnotationType(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public static ContainerAnnotationType fromContent(String content) {
        return types.get(content);
    }
}
