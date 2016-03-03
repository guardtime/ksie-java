package com.guardtime.container.packaging;

import java.io.InputStream;

public interface MimeType {
    String getUri();

    InputStream getInputStream();
}
