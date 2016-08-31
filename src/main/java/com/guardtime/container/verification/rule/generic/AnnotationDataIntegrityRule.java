package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;

/**
 * This rule verifies that the annotation data has not been corrupted.
 */
public class AnnotationDataIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    public AnnotationDataIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) {
        FileReference reference = verifiable.getRight();
        SignatureContent signatureContent = verifiable.getLeft();
        RuleState ruleState = getRuleState(reference);
        VerificationResult verificationResult = getFailureVerificationResult();
        GenericVerificationResult result;

        AnnotationDataReference annotationDataReference = getAnnotationDataReference(reference, signatureContent);
        String annotationDataUri = annotationDataReference.getUri();
        ContainerAnnotation annotation = signatureContent.getAnnotations().get(annotationDataUri);

        try {
            DataHash expectedHash = annotationDataReference.getHash();
            DataHash realHash = annotation.getDataHash(expectedHash.getAlgorithm());
            if (expectedHash.equals(realHash)) {
                verificationResult = VerificationResult.OK;
            }
            result = new GenericVerificationResult(verificationResult, this, annotationDataUri);
        } catch (IOException e) {
            LOGGER.info("Verifying annotation data failed!", e);
            result = new GenericVerificationResult(verificationResult, this, annotationDataUri, e);
        }

        if (!verificationResult.equals(VerificationResult.OK) && ruleState.equals(RuleState.IGNORE)) {
            // We drop non OK for ignored
            return;
        }

        holder.addResult(result);
    }

    private AnnotationDataReference getAnnotationDataReference(FileReference reference, SignatureContent signatureContent) {
        String manifestUri = reference.getUri();
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        return manifest.getAnnotationReference();
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_ANNOTATION_DATA";
    }

    @Override
    public String getErrorMessage() {
        return "Annotation data hash mismatch.";
    }
}
