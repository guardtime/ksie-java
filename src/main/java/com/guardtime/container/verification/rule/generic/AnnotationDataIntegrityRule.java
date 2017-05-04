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
import com.guardtime.container.verification.result.VerificationResultFilter;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotation data has not been corrupted.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.container.manifest.AnnotationsManifest} and {@link SingleAnnotationManifest} and annotation data.
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
            if (dataExistenceRuleFailed(holder, annotationDataReference.getUri())) continue;

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
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) && (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                        result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName()));
            }
        };
    }

    private AnnotationDataReference getAnnotationDataReference(String manifestUri, SignatureContent signatureContent) {
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        return manifest.getAnnotationReference();
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    private boolean manifestExistenceOrIntegrityRuleFailed(ResultHolder holder, final String manifestUri) {
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return result.getTestedElementPath().equals(manifestUri) &&
                        (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) ||
                                result.getRuleName().equals(KSIE_VERIFY_ANNOTATION.getName()));
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 2).equals(OK);
    }

    private boolean dataExistenceRuleFailed(ResultHolder holder, final String dataUri) {
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return (result.getTestedElementPath().equals(dataUri) &&
                        result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName()));
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 1).equals(OK);
    }
}
