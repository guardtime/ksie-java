package com.guardtime.container.manifest;


import java.io.IOException;
import java.io.InputStream;

public interface AnnotationsManifest {

    InputStream getInputStream() throws IOException;

    String getUri();

}
