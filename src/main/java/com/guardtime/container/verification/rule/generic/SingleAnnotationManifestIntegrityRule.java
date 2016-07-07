package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
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
        RuleVerificationResult verificationResult;
        VerificationResult result = getFailureVerificationResult();
        String manifestUri = verifiable.getRight().getUri();
        try {
            Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
            SingleAnnotationManifest manifest = singleAnnotationManifests.get(manifestUri);
            for (DataHash expectedHash : verifiable.getRight().getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() != HashAlgorithm.Status.NORMAL) {
                    LOGGER.info("Will not perform hash verification for '{}' because algorithm status is '{}'", expectedHash, expectedHash.getAlgorithm().getStatus());
                    continue; // Skip not implemented or not trusted hashes
                }
                DataHash realHash = manifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(realHash)) {
                    result = VerificationResult.OK;
                    LOGGER.info("Generated hash matches hash in reference. Hash: '{}'", realHash);
                } else {
                    LOGGER.warn("Generated hash does not match hash in reference. Expecting '{}', got '{}'", expectedHash, realHash);
                }
            }
            verificationResult = new TerminatingVerificationResult(result, this, manifestUri);
        } catch (IOException e) {
            LOGGER.info("Verifying annotation meta-data failed!", e);
            verificationResult = new TerminatingVerificationResult(result, this, manifestUri, e);
        }
        return Arrays.asList(verificationResult);
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
