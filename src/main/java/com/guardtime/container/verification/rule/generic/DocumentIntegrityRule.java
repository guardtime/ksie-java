package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.Map;

/**
 * This rule verifies that the {@link ContainerDocument} being tested has not been corrupted.
 */
public class DocumentIntegrityRule extends AbstractRule<Pair<FileReference, SignatureContent>> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_HASH.name();
    private final MultiHashElementIntegrityRule integrityRule;

    public DocumentIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        integrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<FileReference, SignatureContent> verifiable) throws RuleTerminatingException {
        FileReference documentReference = verifiable.getLeft();
        Map<String, ContainerDocument> documents = verifiable.getRight().getDocuments();
        MultiHashElement document = documents.get(documentReference.getUri());

        try {
            integrityRule.verify(holder, Pair.of(document, documentReference));
        } catch (RuleTerminatingException e) {
            LOGGER.info("Data file hash verification failed with message: '{}'", e.getMessage());
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
