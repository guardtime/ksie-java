package com.guardtime.container.manifest;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataFilesManifest {

    InputStream getInputStream() throws IOException;

    List<? extends FileReference> getDataFileReferences();

}
