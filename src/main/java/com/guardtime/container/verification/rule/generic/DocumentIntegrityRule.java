package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.result.ResultHolder.findHighestPriorityResult;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;

/**
 * This rule verifies that the {@link ContainerDocument} being tested has not been corrupted.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.container.manifest.DocumentsManifest} and document existence.
 */
public class DocumentIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_HASH.getName();
    private final MultiHashElementIntegrityRule integrityRule;

    public DocumentIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        integrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for (FileReference documentReference : verifiable.getDocumentsManifest().getRight().getDocumentReferences()) {
            String uri = documentReference.getUri();
            if (existenceRuleFailed(holder.getResults(verifiable), uri)) continue;

            MultiHashElement document = verifiable.getDocuments().get(uri);

            ResultHolder tempHolder = new ResultHolder();
            try {
                integrityRule.verify(tempHolder, Pair.of(document, documentReference));
            } catch (RuleTerminatingException e) {
                LOGGER.info("Data file hash verification failed with message: '{}'", e.getMessage());
            } finally {
                holder.addResults(verifiable, tempHolder.getResults());
            }
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

    @Override
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder, SignatureContent verifiable) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults(verifiable)) {
            if (result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName()) ||
                    result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST.getName())) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }

    private boolean existenceRuleFailed(List<RuleVerificationResult> results, String documentUri) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : results) {
            if (result.getRuleName().equals(KSIE_VERIFY_DATA_EXISTS.getName()) &&
                    result.getTestedElementPath().equals(documentUri)) {
                filteredResults.add(result);
            }
        }
        return filteredResults.isEmpty() || !findHighestPriorityResult(filteredResults).equals(VerificationResult.OK);
    }

}
