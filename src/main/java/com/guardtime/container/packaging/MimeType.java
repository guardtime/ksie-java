package com.guardtime.container.packaging;

import java.io.IOException;
import java.io.InputStream;

/**
 * MIME type in container.
 */
public interface MimeType {
    String MIME_TYPE_ENTRY_NAME = "mimetype";

    String getUri();

    /**
     * Returns InputStream containing the MIME type data.
     * @throws IOException when the stream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;
}
