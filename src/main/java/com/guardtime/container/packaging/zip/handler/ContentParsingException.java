package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;

public class ContentParsingException extends BlockChainContainerException {
    public ContentParsingException(Throwable cause) {
        super(cause);
    }

    public ContentParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentParsingException(String message) {
        super(message);
    }
}
