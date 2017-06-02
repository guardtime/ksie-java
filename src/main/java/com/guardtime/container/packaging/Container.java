package com.guardtime.container.packaging;

import com.guardtime.container.document.ParsedContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.packaging.parsing.store.ParsingStore;
import com.guardtime.container.packaging.parsing.store.ParsingStoreException;
import com.guardtime.container.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.packaging.ContainerMergingVerifier.verifyNewSignatureContentIsAcceptable;
import static com.guardtime.container.packaging.ContainerMergingVerifier.verifySameMimeType;
import static com.guardtime.container.packaging.ContainerMergingVerifier.verifyUniqueUnknownFiles;
import static com.guardtime.container.packaging.ContainerMergingVerifier.verifyUniqueness;

/**
 * Container that encompasses documents, annotations and structure elements that links the annotations to the documents
 * and signatures that validate the content of the container.
 */
public class Container implements AutoCloseable {

    private ParsingStore parsingStore;
    private final ContainerWriter writer;
    private List<SignatureContent> signatureContents = new LinkedList<>();
    private MimeType mimeType;
    private boolean closed = false;
    private List<UnknownDocument> unknownFiles = new LinkedList<>();

    public Container(SignatureContent signatureContent, MimeType mimeType, ContainerWriter writer) {
        this(Collections.singletonList(signatureContent), Collections.<UnknownDocument>emptyList(), mimeType, writer, null);
    }

    public Container(List<SignatureContent> contents, List<UnknownDocument> unknownFiles, MimeType mimeType,
                     ContainerWriter writer, ParsingStore store) {
        Util.notNull(contents, "Signature contents");
        Util.notNull(unknownFiles, "Unknown files");
        this.signatureContents.addAll(contents);
        this.unknownFiles.addAll(unknownFiles);
        this.mimeType = mimeType;
        this.parsingStore = store;
        this.writer = writer;
    }

    protected Container(Container original) {
        this(
                original.getSignatureContents(),
                original.getUnknownFiles(),
                original.getMimeType(),
                original.getWriter(),
                original.getParsingStore()
        );
    }

    /**
     * Returns list of {@link SignatureContent} contained in this container.
     */
    public List<SignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(signatureContents);
    }

    /**
     * Returns the {@link SignatureContent} at {@param index} and removes it from this {@link Container}.
     */
    public SignatureContent removeSignatureContent(int index) {
        return signatureContents.remove(index);
    }

    /**
     * Writes data to provided stream.
     * @param output    OutputStream to write to. This stream will be closed after writing data to it.
     * @throws IOException When writing to the stream fails for any reason.
     */
    public void writeTo(OutputStream output) throws IOException {
        if (closed) {
            throw new IOException("Can't write closed object!");
        }
        writer.write(this, output);
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * Returns List of all {@link UnknownDocument} that were not associated with any structure elements or signatures but were
     * contained in the {@link Container}
     */
    public List<UnknownDocument> getUnknownFiles() {
        return Collections.unmodifiableList(unknownFiles);
    }

    /**
     * Closes the container and all {@link com.guardtime.container.document.ContainerDocument}s and
     * {@link com.guardtime.container.annotation.ContainerAnnotation}s in the container.
     * NB! This will close {@link com.guardtime.container.document.ContainerDocument}s and
     * {@link com.guardtime.container.annotation.ContainerAnnotation}s added during creation as well.
     */
    @Override
    public void close() throws Exception {
        for (SignatureContent content : getSignatureContents()) {
            content.close();
        }
        for (UnknownDocument f : getUnknownFiles()) {
            f.close();
        }
        if (parsingStore != null) {
            this.parsingStore.close();
        }
        this.closed = true;
    }

    /**
     * Adds the {@link SignatureContent} to this {@link Container}. Also takes ownership of the resources associated with the
     * {@link SignatureContent} and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws ContainerMergingException when the {@link SignatureContent} can not be added into the {@link Container} due to
     * clashing file paths or any other reason.
     */
    public void add(SignatureContent content) throws ContainerMergingException {
        verifyNewSignatureContentIsAcceptable(content, signatureContents);
        verifyUniqueness(content, signatureContents);
        signatureContents.add(content);
    }

    /**
     * Adds all {@link SignatureContent}s from input {@link Container}. Also takes ownership of the resources associated with the
     * {@link Container} and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws ContainerMergingException when any {@link SignatureContent} can not be added into the {@link Container} due to
     * clashing file paths or any other reason.
     */
    public void add(Container container) throws ContainerMergingException {
        verifySameMimeType(container, this);
        verifyUniqueUnknownFiles(container, this);
        int i = container.getSignatureContents().size();
        while (i > 0) {
            add(container.removeSignatureContent(0));
            i--;
        }

        if (parsingStore != null && container.getParsingStore() != null) {
            for (UnknownDocument unknownDocument : container.getUnknownFiles()) {
                unknownFiles.add(new ParsedContainerDocument(
                        parsingStore,
                        unknownDocument.getFileName(),
                        unknownDocument.getMimeType(),
                        unknownDocument.getFileName()
                ));
            }
            try {
                parsingStore.transferFrom(container.getParsingStore());
            } catch (ParsingStoreException e) {
                throw new ContainerMergingException("Failed to take control of parsed data!", e);
            }
        } else if (container.getParsingStore() != null) {
            parsingStore = container.getParsingStore();
            unknownFiles = container.removeAllUnknownFiles();
        }
    }

    private List<UnknownDocument> removeAllUnknownFiles() {
        List<UnknownDocument> returnable = unknownFiles;
        unknownFiles = Collections.emptyList();
        return returnable;
    }

    /**
     * Adds all {@link SignatureContent}s to this {@link Container}. Also takes ownership of the resources associated with the
     * {@link SignatureContent}s and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws ContainerMergingException when any {@link SignatureContent} can not be added into the {@link Container} due to
     * clashing file paths or any other reason.
     */
    public void addAll(Collection<SignatureContent> contents) throws ContainerMergingException {
        List<SignatureContent> original = new LinkedList<>(signatureContents);
        try {
            for (SignatureContent content : contents) {
                add(content);
            }
        } catch (Exception e) {
            this.signatureContents = original;
            throw e;
        }
    }

    protected ParsingStore getParsingStore() {
        return parsingStore;
    }

    protected ContainerWriter getWriter() {
        return writer;
    }
}
