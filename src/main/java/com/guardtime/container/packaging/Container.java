package com.guardtime.container.packaging;

import com.guardtime.container.document.UnknownDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Container that encompasses documents, annotations and structure elements that links the annotations to the documents
 * and signatures that validate the content of the container.
 */
public interface Container extends AutoCloseable {
    /**
     * Returns list of {@link SignatureContent} contained in this container.
     */
    List<? extends SignatureContent> getSignatureContents();

    /**
     * Writes data to provided stream.
     *
     * @param output    OutputStream to write to. This stream will be closed after writing data to it.
     * @throws IOException When writing to the stream fails for any reason.
     */
    void writeTo(OutputStream output) throws IOException;

    MimeType getMimeType();

    /**
     * Returns List of all {@link UnknownDocument} that were not associated with any structure elements or signatures but were
     * contained in the {@link Container}
     */
    List<UnknownDocument> getUnknownFiles();

}
