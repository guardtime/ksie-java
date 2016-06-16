package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This rule verifies that the annotation data has not been corrupted.
 */
public class AnnotationDataIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> implements Rule<Pair<SignatureContent, FileReference>> {

    public AnnotationDataIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationDataIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<SignatureContent, FileReference> verifiable) {
        List<RuleVerificationResult> results = new LinkedList<>();
        VerificationResult verificationResult = getFailureVerificationResult();

        String manifestUri = verifiable.getRight().getUri();
        SignatureContent signatureContent = verifiable.getLeft();
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        AnnotationDataReference annotationDataReference = manifest.getAnnotationReference();
        String annotationDataUri = annotationDataReference.getUri();
        ContainerAnnotation annotation = signatureContent.getAnnotations().get(annotationDataUri);

        try {
            DataHash expectedHash = annotationDataReference.getHash();
            DataHash realHash = annotation.getDataHash(expectedHash.getAlgorithm());
            if (expectedHash.equals(realHash)) {
                verificationResult = VerificationResult.OK;
            }
            results.add(new GenericVerificationResult(verificationResult, this, annotationDataUri));
        } catch (IOException e) {
            LOGGER.info("Verifying annotation data failed!", e);
            results.add(new GenericVerificationResult(verificationResult, this, annotationDataUri, e));
        }
        return results;
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
