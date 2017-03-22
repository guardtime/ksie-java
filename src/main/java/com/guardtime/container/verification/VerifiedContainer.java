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
 * Encompasses all results from verifying a {@link Container} Provides easier access to overall result of verification.
 */
public class VerifiedContainer implements Container {
    private final VerificationResult aggregateResult;
    private final ResultHolder resultHolder;
    private final Container container;
    private List<VerifiedSignatureContent> verifiedSignatureContents;

    public VerifiedContainer(Container container, ResultHolder holder) {
        this.container = container;
        this.resultHolder = holder;
        this.aggregateResult = findHighestPriorityResult(resultHolder.getResults());
        updateVerifiedSignatureContent();
    }

    /**
     * Provides access to all the {@link RuleVerificationResult} gathered during verification.
     *
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
        updateVerifiedSignatureContent();
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        container.add(container);
        updateVerifiedSignatureContent();
    }

    @Override
    public void addAll(Collection<SignatureContent> contents) throws ContainerMergingException {
        container.addAll(contents);
        updateVerifiedSignatureContent();
    }

    private void updateVerifiedSignatureContent() {
        List<VerifiedSignatureContent> verifiedContents = new ArrayList<>(container.getSignatureContents().size());
        for(SignatureContent content : container.getSignatureContents()) {
            verifiedContents.add(new VerifiedSignatureContent(content, resultHolder));
        }
        this.verifiedSignatureContents = verifiedContents;
    }

    private VerificationResult findHighestPriorityResult(List<RuleVerificationResult> verificationResults) {
        VerificationResult returnable = VerificationResult.OK;
        for (RuleVerificationResult result : verificationResults) {
            VerificationResult verificationResult = result.getVerificationResult();
            if (verificationResult.isMoreImportantThan(returnable)) {
                returnable = verificationResult;
                if (VerificationResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }
}
