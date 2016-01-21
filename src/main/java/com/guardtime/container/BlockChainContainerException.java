package com.guardtime.container;

public class BlockChainContainerException extends RuntimeException {

    public BlockChainContainerException(Throwable cause) {
        super(cause);
    }

    public BlockChainContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockChainContainerException(String message) {
        super(message);
    }
}
