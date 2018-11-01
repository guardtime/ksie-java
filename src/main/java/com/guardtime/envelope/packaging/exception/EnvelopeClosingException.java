package com.guardtime.envelope.packaging.exception;

import com.guardtime.envelope.EnvelopeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper exception that encompasses any/all {@link Exception} encountered by {@link com.guardtime.envelope.packaging.Envelope}
 * during {@link com.guardtime.envelope.packaging.Envelope#close()}.
 * Wrapped exceptions can be retrieved by calling {@link #getExceptions()}.
 */
public class EnvelopeClosingException extends EnvelopeException {
    private List<Exception> exceptions;

    public EnvelopeClosingException(String message, Collection<Exception> causes) {
        super(message, causes.iterator().next());
        this.exceptions = new ArrayList<>(causes);
    }

    public List<Exception> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }
}
