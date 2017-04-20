package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotmanifest has not been corrupted.
 * It expects to find successful results for rules verifying existence of
 * {@link com.guardtime.container.manifest.AnnotationsManifest}.
 */
public class AnnotationsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION_MANIFEST.getName();
    private final MultiHashElementIntegrityRule integrityRule;

    public AnnotationsManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        integrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        MultiHashElement annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        ResultHolder tempHolder = new ResultHolder();
        try {
            integrityRule.verify(tempHolder, Pair.of(annotationsManifest, annotationsManifestReference));
        } catch (RuleTerminatingException e) {
            LOGGER.info("Annotations manifest hash verification failed with message: '{}'", e.getMessage());
        } finally {
            holder.addResults(verifiable, tempHolder.getResults());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation manifest hash mismatch.";
    }

    @Override
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder, SignatureContent verifiable) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults(verifiable)) {
            if (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName())) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }

}
