package com.guardtime.container.signature;

import com.guardtime.container.ContainerException;

public class SignatureException extends ContainerException {

    public SignatureException(Exception e) {
        super(e);
    }

    public SignatureException(String s) {
        super(s);
    }

}
