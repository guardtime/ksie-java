package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.ContainerException;

public class ContentParsingException extends ContainerException {

    public ContentParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentParsingException(String message) {
        super(message);
    }
}
