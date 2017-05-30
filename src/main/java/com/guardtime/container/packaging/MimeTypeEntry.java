package com.guardtime.container.packaging;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MimeTypeEntry implements MimeType {

    private final String uri;
    private final byte[] content;

    public MimeTypeEntry(String uri, byte[] content) {
        this.uri = uri;
        this.content = content;
    }

    public String getUri() {
        return uri;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

}
