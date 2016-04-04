package com.guardtime.container.packaging;

import com.guardtime.container.ContainerFileElement;

import java.io.IOException;
import java.io.InputStream;

public interface MimeType extends ContainerFileElement {
    String getUri();

    InputStream getInputStream() throws IOException;
}
