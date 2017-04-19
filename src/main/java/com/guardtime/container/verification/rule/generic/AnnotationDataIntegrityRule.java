package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.result.ResultHolder.findHighestPriorityResult;
import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotation data has not been corrupted.
 */
public class AnnotationDataIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION_DATA.getName();

    public AnnotationDataIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent signatureContent) {
        AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getSingleAnnotationManifestReferences()) {
            String manifestUri = reference.getUri();

            RuleState ruleState = getRuleState(reference);
            VerificationResult verificationResult = getFailureVerificationResult();
            GenericVerificationResult result;

            if (manifestExistenceOrIntegrityRuleFailed(holder, manifestUri)) continue;
            AnnotationDataReference annotationDataReference = getAnnotationDataReference(manifestUri, signatureContent);
            if(dataExistenceRuleFailed(holder, annotationDataReference.getUri())) continue;

            String annotationDataUri = annotationDataReference.getUri();
            ContainerAnnotation annotation = signatureContent.getAnnotations().get(annotationDataUri);

            try {
                DataHash expectedHash = annotationDataReference.getHash();
                DataHash realHash = annotation.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.getAlgorithm().getStatus() == HashAlgorithm.Status.NORMAL && expectedHash.equals(realHash)) {
                    verificationResult = VerificationResult.OK;
                }
                result = new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), annotationDataUri);
            } catch (DataHashException e) {
                LOGGER.info("Verifying annotation data failed!", e);
                result = new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), annotationDataUri, e);
            }

            if (!verificationResult.equals(VerificationResult.OK) && ruleState.equals(RuleState.IGNORE)) {
                // We drop non OK for ignored
                continue;
            }

            holder.addResult(signatureContent, result);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation data hash mismatch.";
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

    private AnnotationDataReference getAnnotationDataReference(String manifestUri, SignatureContent signatureContent) {
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        return manifest.getAnnotationReference();
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    private boolean manifestExistenceOrIntegrityRuleFailed(ResultHolder holder, String manifestUri) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if (result.getTestedElementPath().equals(manifestUri) &&
                    (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) ||
                            result.getRuleName().equals(KSIE_VERIFY_ANNOTATION.getName()))) {
                filteredResults.add(result);
            }
        }
        return filteredResults.size() < 2 || !findHighestPriorityResult(filteredResults).equals(OK);
    }

    private boolean dataExistenceRuleFailed(ResultHolder holder, String dataUri) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if((result.getTestedElementPath().equals(dataUri) &&
                    result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName()))) {
                filteredResults.add(result);
            }
        }
        return filteredResults.isEmpty() || !findHighestPriorityResult(filteredResults).equals(OK);
    }
}
