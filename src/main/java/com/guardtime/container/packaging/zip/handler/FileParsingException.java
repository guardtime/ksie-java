package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;

public class FileParsingException extends BlockChainContainerException {
    public FileParsingException(Throwable cause) {
        super(cause);
    }

    public FileParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileParsingException(String message) {
        super(message);
    }
}
