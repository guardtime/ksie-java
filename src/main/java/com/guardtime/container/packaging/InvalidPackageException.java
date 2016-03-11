package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;

public class InvalidPackageException extends BlockChainContainerException {
    public InvalidPackageException(Throwable cause) {
        super(cause);
    }

    public InvalidPackageException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPackageException(String message) {
        super(message);
    }
}
