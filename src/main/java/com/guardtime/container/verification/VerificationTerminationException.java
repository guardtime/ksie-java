package com.guardtime.container.verification;

import com.guardtime.container.BlockChainContainerException;

public class VerificationTerminationException extends BlockChainContainerException {
    public VerificationTerminationException(String message) {
        super(message);
    }
}
