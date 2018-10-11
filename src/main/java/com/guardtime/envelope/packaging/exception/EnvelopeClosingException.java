package com.guardtime.envelope.packaging.exception;

import com.guardtime.envelope.EnvelopeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper exception that encompasses any/all {@link Exception} encountered by {@link com.guardtime.envelope.packaging.Envelope}
 * during {@link com.guardtime.envelope.packaging.Envelope#close()}.
 * Wrapped exceptions can be retrieved by calling {@link #getExceptions()}.
 */
public class EnvelopeClosingException extends EnvelopeException {
    private List<Throwable> exceptions = new ArrayList<>();

    public EnvelopeClosingException(String message, Throwable cause) {
        this(message);
        addException(cause);
    }

    public EnvelopeClosingException(String message) {
        super(message);
    }

    public void addException(Throwable e) {
        exceptions.add(e);
    }

    public List<Throwable> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }
}
