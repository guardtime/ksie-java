package com.guardtime.container.verification;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Encompasses all results from verifying a {@link Container}.
 * Provides easier access to overall result of verification.
 */
public class VerifiedContainer extends Container {
    private final VerificationResult aggregateResult;
    private final ResultHolder resultHolder;
    private List<VerifiedSignatureContent> verifiedSignatureContents;

    public VerifiedContainer(Container container, ResultHolder holder) {
        super(
                container.getSignatureContents(),
                container.getUnknownFiles(),
                container.getMimeType(),
                container.getWriter(),
                null
        );
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

    public List<VerifiedSignatureContent> getVerifiedSignatureContents() {
        return Collections.unmodifiableList(verifiedSignatureContents);
    }

    public void add(SignatureContent content) throws ContainerMergingException {
        super.add(content);
        wrapSignatureContents();
    }

    public void add(Container container) throws ContainerMergingException {
        super.add(container);
        wrapSignatureContents();
    }

    public void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException {
        super.addAll(contents);
        wrapSignatureContents();
    }

    private void wrapSignatureContents() {
        List<SignatureContent> originalSignatureContents = super.getSignatureContents();
        List<VerifiedSignatureContent> verifiedContents = new ArrayList<>(originalSignatureContents.size());
        for(SignatureContent content : originalSignatureContents) {
            verifiedContents.add(new VerifiedSignatureContent(content, resultHolder));
        }
        this.verifiedSignatureContents = verifiedContents;
    }

}
