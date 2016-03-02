package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.BCCMimeType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

class MimeTypeEntry implements BCCMimeType {

    public static final String ENTRY_NAME_MIME_TYPE = "mimetype";
    public static final String CONTAINER_MIME_TYPE = "application/guardtime.ksie10+zip";

    public String getUri() {
        return ENTRY_NAME_MIME_TYPE;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(CONTAINER_MIME_TYPE.getBytes(Charset.forName("UTF-8")));
    }

}
