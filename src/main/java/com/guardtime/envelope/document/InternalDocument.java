package com.guardtime.envelope.document;

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

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
        super(MIME_TYPE, extractPath(element));
        this.element = element;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = element.getInputStream();
        if (inputStream == null) {
            throw new IOException("No data for stream!");
        }
        return inputStream;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        try {
            return element.getDataHash(algorithm);
        } catch (DataHashException e) {
            return super.getDataHash(algorithm);
        }
    }

    private static String extractPath(EnvelopeElement element) {
        Util.notNull(element, "EnvelopeElement");
        return element.getPath();
    }

}
