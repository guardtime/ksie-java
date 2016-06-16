package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    public SingleAnnotationManifestIntegrityRule() {
        this(RuleState.FAIL);
    }

    public SingleAnnotationManifestIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<SignatureContent, FileReference> verifiable) {
        VerificationResult result = getFailureVerificationResult();
        String manifestUri = verifiable.getRight().getUri();
        try {
            Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
            SingleAnnotationManifest manifest = singleAnnotationManifests.get(manifestUri);
            for(DataHash expectedHash : verifiable.getRight().getHashList()) {
                if(expectedHash.getAlgorithm().getStatus()!= HashAlgorithm.Status.NORMAL) continue; // Skip not implemented or not trusted
                DataHash realHash = manifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(realHash)) {
                    result = VerificationResult.OK;
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Verifying annotation meta-data failed!", e);
        }
        TerminatingVerificationResult verificationResult = new TerminatingVerificationResult(result, this, manifestUri);
        return Arrays.asList((RuleVerificationResult) verificationResult);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_ANNOTATION";
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data hash mismatch.";
    }
}
