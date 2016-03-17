package com.guardtime.container.util;

import com.guardtime.container.BlockChainContainerException;

public class DataHashException extends BlockChainContainerException {
    public DataHashException(String message) {
        super(message);
    }
}
