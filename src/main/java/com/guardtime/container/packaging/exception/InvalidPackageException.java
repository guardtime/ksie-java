package com.guardtime.container.packaging.exception;

import com.guardtime.container.ContainerException;

public class InvalidPackageException extends ContainerException {

    public InvalidPackageException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPackageException(String message) {
        super(message);
    }

}
