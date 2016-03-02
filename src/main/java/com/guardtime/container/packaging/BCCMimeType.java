package com.guardtime.container.packaging;

import java.io.InputStream;

public interface BCCMimeType {
    String getUri();

    InputStream getInputStream();
}
