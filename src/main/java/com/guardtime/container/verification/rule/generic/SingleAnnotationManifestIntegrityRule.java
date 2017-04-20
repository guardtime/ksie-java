package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.result.ResultHolder.findHighestPriorityResult;
import static com.guardtime.container.verification.result.VerificationResult.NOK;
import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.container.manifest.AnnotationsManifest} and existence of {@link SingleAnnotationManifest}.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION.getName();
    private final MultiHashElementIntegrityRule multiHashElementIntegrityRule;

    public SingleAnnotationManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        multiHashElementIntegrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for (FileReference reference : verifiable.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences()) {
            String singleAnnotationManifestUri = reference.getUri();
            if (existenceRuleFailed(holder, singleAnnotationManifestUri)) continue;

            SingleAnnotationManifest manifest = verifiable.getSingleAnnotationManifests().get(singleAnnotationManifestUri);
            Pair<String, DocumentsManifest> documentsManifestPair = verifiable.getDocumentsManifest();
            FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
            ResultHolder tempHolder = new ResultHolder();
            try {
                multiHashElementIntegrityRule.verify(tempHolder, Pair.of((MultiHashElement) manifest, reference));

                if (!documentsManifestPair.getLeft().equals(documentsManifestReference.getUri())) {
                    tempHolder.addResult(
                            verifiable,
                            new GenericVerificationResult(NOK, getName(), getErrorMessage(), reference.getUri())
                    );
                    throw new RuleTerminatingException("Documents manifest path mismatch found.");
                }
                multiHashElementIntegrityRule.verifyRule(
                        tempHolder,
                        Pair.of((MultiHashElement) documentsManifestPair.getRight(), documentsManifestReference)
                );
            } catch (RuleTerminatingException e) {
                // we do not let this through
                LOGGER.info("Annotation manifest hash verification failed with message: '{}'", e.getMessage());
            } finally {
                RuleState ruleState = getRuleState(reference);
                for (RuleVerificationResult result : tempHolder.getResults()) {
                    if (!result.getVerificationResult().equals(OK) && ruleState.equals(RuleState.IGNORE)) {
                        // We ignore problems
                        continue;
                    }
                    holder.addResult(verifiable, result);
                }
            }
        }
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.FULLY_REMOVABLE) ? RuleState.IGNORE : state;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data mismatch.";
    }

    @Override
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder, SignatureContent verifiable) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults(verifiable)) {
            if (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                    result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName())) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }

    private boolean existenceRuleFailed(ResultHolder holder, String uri) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) &&
                    result.getTestedElementPath().equals(uri)) {
                filteredResults.add(result);
            }
        }
        return filteredResults.isEmpty() || !findHighestPriorityResult(filteredResults).equals(OK);
    }

}
