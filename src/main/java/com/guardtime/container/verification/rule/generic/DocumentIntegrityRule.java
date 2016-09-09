package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.List;

/**
 * This rule verifies that the {@link ContainerDocument} being tested has not been corrupted.
 */
public class DocumentIntegrityRule extends AbstractRule<Pair<FileReference, SignatureContent>> {

    public DocumentIntegrityRule() {
        this(RuleState.FAIL);
    }

    public DocumentIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<FileReference, SignatureContent> verifiable) {
        FileReference fileReference = verifiable.getLeft();
        String documentUri = fileReference.getUri();
        ContainerDocument document = verifiable.getRight().getDocuments().get(documentUri);
        return getFileReferenceHashListVerificationResult(document, fileReference);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_DATA_HASH";
    }

    @Override
    public String getErrorMessage() {
        return "Data file hash mismatch.";
    }
}
