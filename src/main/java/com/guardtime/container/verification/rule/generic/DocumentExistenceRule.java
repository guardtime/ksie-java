package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.Arrays;
import java.util.List;

/**
 * This rule verifies that the tested {@link ContainerDocument} is indeed present in the {@link
 * com.guardtime.container.packaging.Container}
 */
public class DocumentExistenceRule extends AbstractRule<Pair<FileReference, SignatureContent>> implements Rule<Pair<FileReference, SignatureContent>> {

    public DocumentExistenceRule() {
        this(RuleState.FAIL);
    }

    public DocumentExistenceRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<FileReference, SignatureContent> verifiable) {
        VerificationResult result = getFailureVerificationResult();
        String documentUri = verifiable.getLeft().getUri();
        ContainerDocument document = verifiable.getRight().getDocuments().get(documentUri);
        if (document != null && !(document instanceof EmptyContainerDocument)) {
            result = VerificationResult.OK;
        }
        RuleVerificationResult verificationResult = new TerminatingVerificationResult(result, this, documentUri);
        return Arrays.asList(verificationResult);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_DATA_EXISTS";
    }

    @Override
    public String getErrorMessage() {
        return "Signed file does not exist in the container.";
    }
}
