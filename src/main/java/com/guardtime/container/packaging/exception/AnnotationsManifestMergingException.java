package com.guardtime.container.packaging.exception;

public class AnnotationsManifestMergingException extends ContainerMergingException {
    public AnnotationsManifestMergingException(String path) {
        super("New SignatureContent has clashing AnnotationsManifest! Path: " + path);
    }
}
