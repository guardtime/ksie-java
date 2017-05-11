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

package com.guardtime.container.verification;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Encompasses all results from verifying a {@link Container}.
 * Provides easier access to overall result of verification.
 */
public class VerifiedContainer implements Container {
    private final VerificationResult aggregateResult;
    private final ResultHolder resultHolder;
    private final Container container;
    private List<VerifiedSignatureContent> verifiedSignatureContents;

    public VerifiedContainer(Container container, ResultHolder holder) {
        this.container = container;
        this.resultHolder = holder;
        this.aggregateResult = resultHolder.getAggregatedResult();
        wrapSignatureContents();
    }

    /**
     * Provides access to all the {@link RuleVerificationResult} gathered during verification.
     * @return List of {@link RuleVerificationResult}
     */
    public List<RuleVerificationResult> getResults() {
        return resultHolder.getResults();
    }

    /**
     * Provides access to the overall {@link VerificationResult} of the verification.
     */
    public VerificationResult getVerificationResult() {
        return aggregateResult;
    }

    @Override
    public List<VerifiedSignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(verifiedSignatureContents);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        container.writeTo(output);
    }

    @Override
    public MimeType getMimeType() {
        return container.getMimeType();
    }

    @Override
    public List<UnknownDocument> getUnknownFiles() {
        return container.getUnknownFiles();
    }

    @Override
    public void close() throws Exception {
        container.close();
    }

    @Override
    public void add(SignatureContent content) throws ContainerMergingException {
        container.add(content);
        wrapSignatureContents();
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        container.add(container);
        wrapSignatureContents();
    }

    @Override
    public void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException {
        container.addAll(contents);
        wrapSignatureContents();
    }

    private void wrapSignatureContents() {
        List<VerifiedSignatureContent> verifiedContents = new ArrayList<>(container.getSignatureContents().size());
        for(SignatureContent content : container.getSignatureContents()) {
            verifiedContents.add(new VerifiedSignatureContent(content, resultHolder));
        }
        this.verifiedSignatureContents = verifiedContents;
    }

}
