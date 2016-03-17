package com.guardtime.container.packaging;

import java.io.IOException;
import java.io.InputStream;

public interface MimeType {
    String getUri();

    InputStream getInputStream() throws IOException;
}
