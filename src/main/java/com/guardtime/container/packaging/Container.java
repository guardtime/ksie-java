package com.guardtime.container.packaging;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.exception.ContainerMergingException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
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

    /**
     * Closes the container and all {@link com.guardtime.container.document.ContainerDocument}s and
     * {@link com.guardtime.container.annotation.ContainerAnnotation}s in the container.
     * NB! This will close {@link com.guardtime.container.document.ContainerDocument}s and
     * {@link com.guardtime.container.annotation.ContainerAnnotation}s added during creation as well.
     */
    @Override
    void close() throws Exception;

    /**
     * Adds the {@link SignatureContent} to this {@link Container}. Also takes ownership of the resources associated with the
     * {@link SignatureContent} and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws ContainerMergingException when the {@link SignatureContent} can not be added into the {@link Container} due to
     * clashing file paths or any other reason.
     */
    void add(SignatureContent content) throws ContainerMergingException;

    /**
     * Adds all {@link SignatureContent}s from input {@link Container}. Also takes ownership of the resources associated with the
     * {@link Container} and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws ContainerMergingException when any {@link SignatureContent} can not be added into the {@link Container} due to
     * clashing file paths or any other reason.
     */
    void add(Container container) throws ContainerMergingException;

    /**
     * Adds all {@link SignatureContent}s to this {@link Container}. Also takes ownership of the resources associated with the
     * {@link SignatureContent}s and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws ContainerMergingException when any {@link SignatureContent} can not be added into the {@link Container} due to
     * clashing file paths or any other reason.
     */
    void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException;
}
