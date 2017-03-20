package com.guardtime.container.packaging.exception;

import java.io.IOException;

public class MimeTypeMergingException extends ContainerMergingException {
    public MimeTypeMergingException(String s) {
        super(s);
    }

    public MimeTypeMergingException(String s, IOException e) {
        super(s, e);
    }
}
