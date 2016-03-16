package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;

public class InvalidPackageException extends BlockChainContainerException {

    public InvalidPackageException(String message, Throwable cause) {
        super(message, cause);
    }

}
