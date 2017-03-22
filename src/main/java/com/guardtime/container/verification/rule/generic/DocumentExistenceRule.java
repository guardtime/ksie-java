package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;

/**
 * This rule verifies that the tested {@link ContainerDocument} is indeed present in the {@link
 * com.guardtime.container.packaging.Container}
 */
public class DocumentExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_EXISTS.getName();

    public DocumentExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for (FileReference documentReference : verifiable.getDocumentsManifest().getRight().getDocumentReferences()) {
            VerificationResult result = getFailureVerificationResult();
            String documentUri = documentReference.getUri();
            ContainerDocument document = verifiable.getDocuments().get(documentUri);
            if (document != null && !(document instanceof EmptyContainerDocument)) {
                result = VerificationResult.OK;
            }
            holder.addResult(new GenericVerificationResult(result, getName(), getErrorMessage(), documentUri));
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

    @Override
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if (result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName()) ||
                    result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST.getName())) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }
}
