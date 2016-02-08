package com.guardtime.container.signature;

import com.guardtime.container.BlockChainContainerException;

public class SignatureException extends BlockChainContainerException {

    public SignatureException(Exception e) {
        super(e);
    }

    // TODO add type?

}
