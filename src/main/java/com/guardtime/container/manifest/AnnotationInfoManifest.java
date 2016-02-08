package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;

public interface AnnotationInfoManifest {


    InputStream getInputStream() throws IOException;

    String getUri();
}
