package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;

public interface AnnotationInfoManifest {

    AnnotationReference getAnnotationReference();

    FileReference getDataManifestReference();

    InputStream getInputStream() throws IOException;

    boolean writable();

}
