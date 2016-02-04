package com.guardtime.container.manifest;


import java.io.IOException;
import java.io.InputStream;

public interface DataFilesManifest {

    String getUri();

    InputStream getInputStream() throws IOException;

}
