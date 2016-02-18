package com.guardtime.container.manifest;


import com.guardtime.container.manifest.reference.AnnotationInfoManifestReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface AnnotationsManifest {

    InputStream getInputStream() throws IOException;

    String getUri();

    List<? extends AnnotationInfoManifestReference> getAnnotationManifestReferences();

}
