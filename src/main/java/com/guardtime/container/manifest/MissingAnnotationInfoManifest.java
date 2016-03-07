package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;

public class MissingAnnotationInfoManifest implements AnnotationInfoManifest {
    @Override
    public AnnotationReference getAnnotationReference() {
        return null;
    }

    @Override
    public FileReference getDataManifestReference() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public boolean writable() {
        return false;
    }
}
