package com.guardtime.container.packaging;

import com.guardtime.container.ContainerFileElement;

import java.io.IOException;
import java.io.InputStream;

/**
 * MIME type in container.
 */
public interface MimeType extends ContainerFileElement {

    String getUri();

    /**
     * Returns InputStream containing the MIME type data.
     * @throws IOException when the stream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;
}
