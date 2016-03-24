package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface AnnotationsManifest {

    List<? extends FileReference> getAnnotationInfoManifestReferences();

    InputStream getInputStream() throws IOException;

}
