package com.guardtime.container.packaging.exception;

public class DocumentsManifestMergingException extends ContainerMergingException {
    public DocumentsManifestMergingException(String path) {
        super("New SignatureContent has clashing DocumentsManifest! Path: " + path);
    }
}
