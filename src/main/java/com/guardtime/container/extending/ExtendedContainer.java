package com.guardtime.container.extending;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerMergingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper for a {@link Container} that has been processed by {@link ContainerSignatureExtender}.
 * Provides helper methods to view extending status.
 */
public class ExtendedContainer extends Container {

    private List<ExtendedSignatureContent> extendedSignatureContents;

    public ExtendedContainer(Container original) {
        super(original);
        wrapSignatureContents();
    }

    /**
     * Returns true if all {@link SignatureContent}s of this {@link Container} are extended.
     */
    public boolean isFullyExtended() {
        for (ExtendedSignatureContent content : extendedSignatureContents) {
            if (!content.isExtended()) {
                return false;
            }
        }
        return true;
    }

    public List<ExtendedSignatureContent> getExtendedSignatureContents() {
        return Collections.unmodifiableList(extendedSignatureContents);
    }

    @Override
    public void add(SignatureContent content) throws ContainerMergingException {
        super.add(content);
        wrapSignatureContents();
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        super.add(container);
        wrapSignatureContents();
    }

    @Override
    public void addAll(Collection<SignatureContent> contents) throws ContainerMergingException {
        super.addAll(contents);
        wrapSignatureContents();
    }

    private void wrapSignatureContents() {
        List<SignatureContent> originalSignatureContents = getSignatureContents();
        List<ExtendedSignatureContent> extendedContents = new ArrayList<>(originalSignatureContents.size());
        for (SignatureContent content : originalSignatureContents) {
            extendedContents.add(new ExtendedSignatureContent(content));
        }
        this.extendedSignatureContents = extendedContents;
    }
}
