package com.guardtime.container.annotation;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.guardtime.container.util.Util.notNull;

/**
 * Annotation that is based on a String as the data source.
 */
public class StringContainerAnnotation extends AbstractContainerAnnotation {

    private final String content;

    public StringContainerAnnotation(ContainerAnnotationType type, String content, String domain) {
        super(domain, type);
        notNull(content, "Content");
        this.content = content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
