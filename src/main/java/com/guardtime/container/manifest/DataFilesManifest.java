package com.guardtime.container.manifest;


import com.guardtime.container.manifest.reference.DataFileReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataFilesManifest {

    String getUri();

    InputStream getInputStream() throws IOException;

    List<? extends DataFileReference> getDataFileReferences();

}
