package com.guardtime.container.packaging.exception;

public class DocumentMergingException extends ContainerMergingException {
    public DocumentMergingException(String path) {
        super("New SignatureContent has clashing name for ContainerDocument! Path: " + path);
    }
}
