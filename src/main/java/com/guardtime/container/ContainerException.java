package com.guardtime.container;

public class ContainerException extends Exception {

    public ContainerException(Throwable cause) {
        super(cause);
    }

    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContainerException(String message) {
        super(message);
    }
}
