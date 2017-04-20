package com.guardtime.container.extending;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerMergingException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper for a {@link Container} that has been processed by {@link ContainerSignatureExtender}.
 * Provides helper methods to view extending status.
 */
public class ExtendedContainer implements Container {
    private final Container wrappedContainer;
    private List<ExtendedSignatureContent> extendedSignatureContents;

    public ExtendedContainer(Container original) {
        this.wrappedContainer = original;
        updateExtendedSignatureContents();
    }

    /**
     * Returns true if all {@link SignatureContent}s of this {@link Container} are extended.
     */
    public boolean isExtended() {
        boolean extended = true;
        for (ExtendedSignatureContent content : extendedSignatureContents) {
            if (!content.isExtended()) {
                extended = false;
            }
        }
        return extended;
    }

    @Override
    public List<ExtendedSignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(extendedSignatureContents);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        wrappedContainer.writeTo(output);
    }

    @Override
    public MimeType getMimeType() {
        return wrappedContainer.getMimeType();
    }

    @Override
    public List<UnknownDocument> getUnknownFiles() {
        return wrappedContainer.getUnknownFiles();
    }

    @Override
    public void close() throws Exception {
        wrappedContainer.close();
    }

    @Override
    public void add(SignatureContent content) throws ContainerMergingException {
        wrappedContainer.add(content);
        updateExtendedSignatureContents();
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        wrappedContainer.add(container);
        updateExtendedSignatureContents();
    }

    @Override
    public void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException {
        wrappedContainer.addAll(contents);
        updateExtendedSignatureContents();
    }

    private void updateExtendedSignatureContents() {
        List<ExtendedSignatureContent> extendedContents = new ArrayList<>(wrappedContainer.getSignatureContents().size());
        for (SignatureContent content : wrappedContainer.getSignatureContents()) {
            extendedContents.add(new ExtendedSignatureContent(content));
        }
        this.extendedSignatureContents = extendedContents;
    }

}
