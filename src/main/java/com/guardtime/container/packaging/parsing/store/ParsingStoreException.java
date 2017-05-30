package com.guardtime.container.packaging.parsing.store;

import com.guardtime.container.ContainerException;

public class ParsingStoreException extends ContainerException {
    public ParsingStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingStoreException(String message) {
        super(message);
    }
}
