package com.guardtime.container.util;

import com.guardtime.container.ContainerException;

import java.io.IOException;

public class DataHashException extends ContainerException {
    public DataHashException(String message) {
        super(message);
    }

    public DataHashException(String message, IOException cause) {
        super(message, cause);
    }
}
