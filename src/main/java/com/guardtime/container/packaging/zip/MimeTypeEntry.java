package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.MimeType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

class MimeTypeEntry implements MimeType {

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

    @Override
    public String toString() {
        return this.getClass().toString() + " {" +
                "uri= \'" + uri + '\'' +
                ", content= \'" + new String(content) + "\'}";
    }
}
