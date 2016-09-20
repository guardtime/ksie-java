package com.guardtime.container.verification.rule;

import com.guardtime.container.ContainerException;

public class RuleTerminatingException extends ContainerException {

    public RuleTerminatingException(String message) {
        super(message);
    }

    public RuleTerminatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
