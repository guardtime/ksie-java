package com.guardtime.container.packaging.exception;

public class ContainerAnnotationMergingException extends ContainerMergingException {
    public ContainerAnnotationMergingException(String path) {
        super("New SignatureContent has clashing Annotation data! Path: " + path);
    }
}
