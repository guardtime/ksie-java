package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.*;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.util.Map;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION.name();

    public SingleAnnotationManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();
        RuleState ruleState = getRuleState(reference);

        GenericVerificationResult verificationResult;
        VerificationResult result = getFailureVerificationResult();
        String manifestUri = reference.getUri();
        try {
            Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
            SingleAnnotationManifest manifest = singleAnnotationManifests.get(manifestUri);
            for (DataHash expectedHash : reference.getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() != HashAlgorithm.Status.NORMAL) {
                    continue; // Skip not implemented or not trusted
                }
                DataHash realHash = manifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(realHash)) {
                    result = VerificationResult.OK;
                }
            }
            verificationResult = new GenericVerificationResult(result, this, manifestUri);
        } catch (IOException e) {
            LOGGER.info("Verifying annotation meta-data failed!", e);
            verificationResult = new GenericVerificationResult(result, this, manifestUri, e);
        }

        if (!result.equals(VerificationResult.OK) && ruleState.equals(RuleState.IGNORE)) {
            // We ignore problems for this manifest
            return;
        }

        holder.addResult(verificationResult);

        if (!result.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("SingleAnnotationManifest integrity could not be verified for '" + manifestUri + "'");
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
        return "Annotation meta-data hash mismatch.";
    }
}
