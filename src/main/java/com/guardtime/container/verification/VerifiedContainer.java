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
    private final Container wrapedContainer;
    private List<VerifiedSignatureContent> verifiedSignatureContents;

    public VerifiedContainer(Container container, ResultHolder holder) {
        this.wrapedContainer = container;
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
        wrapedContainer.writeTo(output);
    }

    @Override
    public MimeType getMimeType() {
        return wrapedContainer.getMimeType();
    }

    @Override
    public List<UnknownDocument> getUnknownFiles() {
        return wrapedContainer.getUnknownFiles();
    }

    @Override
    public void close() throws Exception {
        wrapedContainer.close();
    }

    @Override
    public void add(SignatureContent content) throws ContainerMergingException {
        wrapedContainer.add(content);
        wrapSignatureContents();
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        wrapedContainer.add(container);
        wrapSignatureContents();
    }

    @Override
    public void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException {
        wrapedContainer.addAll(contents);
        wrapSignatureContents();
    }

    private void wrapSignatureContents() {
        List<VerifiedSignatureContent> verifiedContents = new ArrayList<>(wrapedContainer.getSignatureContents().size());
        for(SignatureContent content : wrapedContainer.getSignatureContents()) {
            verifiedContents.add(new VerifiedSignatureContent(content, resultHolder));
        }
        this.verifiedSignatureContents = verifiedContents;
    }

}
