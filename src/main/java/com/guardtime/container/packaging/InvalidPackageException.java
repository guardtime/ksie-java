package com.guardtime.container.packaging;

import com.guardtime.container.ContainerException;

public class InvalidPackageException extends ContainerException {

    public InvalidPackageException(String message, Throwable cause) {
        super(message, cause);
    }

}
