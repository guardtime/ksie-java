package com.guardtime.container.manifest;


import java.io.InputStream;

public interface DataFilesManifest {

    String getUri();

    InputStream getInputStream();

}
