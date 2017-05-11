/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

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
        wrapSignatureContents();
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        wrappedContainer.add(container);
        wrapSignatureContents();
    }

    @Override
    public void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException {
        wrappedContainer.addAll(contents);
        wrapSignatureContents();
    }

    private void wrapSignatureContents() {
        List<ExtendedSignatureContent> extendedContents = new ArrayList<>(wrappedContainer.getSignatureContents().size());
        for (SignatureContent content : wrappedContainer.getSignatureContents()) {
            extendedContents.add(new ExtendedSignatureContent(content));
        }
        this.extendedSignatureContents = extendedContents;
    }

}
