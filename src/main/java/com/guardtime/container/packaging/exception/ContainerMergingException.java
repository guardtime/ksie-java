package com.guardtime.container.packaging.exception;

import com.guardtime.container.ContainerException;

public class ContainerMergingException extends ContainerException {

    public ContainerMergingException(String message) {
        super(message);
    }

    public ContainerMergingException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
