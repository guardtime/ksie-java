package com.guardtime.container.packaging.exception;

public class SingleAnnotationManifestMergingException extends ContainerMergingException {
    public SingleAnnotationManifestMergingException(String path) {
        super("New SignatureContent has clashing SingleAnnotationManifest! Path: " + path);
    }
}
