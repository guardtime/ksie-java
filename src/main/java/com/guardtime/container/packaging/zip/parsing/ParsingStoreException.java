package com.guardtime.container.packaging.zip.parsing;

import com.guardtime.container.ContainerException;

import java.io.IOException;

public class ParsingStoreException extends ContainerException {
    public ParsingStoreException(String message, IOException cause) {
        super(message, cause);
    }

    public ParsingStoreException(String message) {
        super(message);
    }
}
