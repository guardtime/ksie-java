package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleStateProvider;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;

/**
 * This rule verifies that the tested {@link ContainerDocument} is indeed present in the {@link
 * com.guardtime.container.packaging.Container}
 */
public class DocumentExistenceRule extends AbstractRule<Pair<FileReference, SignatureContent>> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_EXISTS.name();

    public DocumentExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<FileReference, SignatureContent> verifiable) throws RuleTerminatingException {
        VerificationResult result = getFailureVerificationResult();
        String documentUri = verifiable.getLeft().getUri();
        ContainerDocument document = verifiable.getRight().getDocuments().get(documentUri);
        if (document != null && !(document instanceof EmptyContainerDocument)) {
            result = VerificationResult.OK;
        }
        holder.addResult(new GenericVerificationResult(result, this, documentUri));

        if (!result.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("Document existence could not be verified for '" + documentUri + "'");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Signed file does not exist in the container.";
    }
}
