package com.guardtime.container.packaging.zip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

class MimeTypeEntry {

    public static final String ENTRY_NAME_MIME_TYPE = "mimetype";
    public static final String CONTAINER_MIME_TYPE = "application/guardtime.ksie10+zip";

    String getUri() {
        return ENTRY_NAME_MIME_TYPE;
    }

    InputStream getInputStream() {
        return new ByteArrayInputStream(CONTAINER_MIME_TYPE.getBytes(Charset.forName("UTF-8")));
    }

}
