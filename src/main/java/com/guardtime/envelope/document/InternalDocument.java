package com.guardtime.envelope.document;

import com.guardtime.envelope.EnvelopeElement;

import java.io.IOException;
import java.io.InputStream;

/**
 * Allows for easily converting an {@link EnvelopeElement} to {@link Document} so it can be used for adding new
 * {@link com.guardtime.envelope.signature.EnvelopeSignature} to {@link com.guardtime.envelope.packaging.Envelope}.
 */

public class InternalDocument extends AbstractDocument {
    private static final String MIME_TYPE = "application/ksie-structure-file"; // non-documented value
    private final EnvelopeElement element;

    public InternalDocument(EnvelopeElement element) {
        super(MIME_TYPE, element.getPath());
        this.element = element;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return element.getInputStream();
    }

}
