package com.guardtime.container.manifest;

import com.guardtime.container.BlockChainContainerException;

public class InvalidManifestException extends BlockChainContainerException {

    public InvalidManifestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidManifestException(String message) {
        super(message);
    }

    public InvalidManifestException(Throwable cause) {
        super(cause);
    }
}
