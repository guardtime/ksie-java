package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleStateProvider;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;

import java.util.Map;

/**
 * This rule verifies that the {@link ContainerDocument} being tested has not been corrupted.
 */
public class DocumentIntegrityRule extends AbstractRule<Pair<FileReference, SignatureContent>> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_HASH.getName();

    public DocumentIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<FileReference, SignatureContent> verifiable) {
        FileReference documentReference = verifiable.getLeft();
        Map<String, ContainerDocument> documents = verifiable.getRight().getDocuments();
        ContainerDocument document = documents.get(documentReference.getUri());
        try {
            verifyMultiHashElement(document, documentReference, holder);
        } catch (RuleTerminatingException e) {
            LOGGER.info(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Data file hash mismatch.";
    }
}
