package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Rule that verifies the hash integrity of {@link DataFilesManifest} as noted in {@link SignatureManifest}.
 */
public class DataFilesManifestIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_DATA_FILES_MANIFEST = "KSIE_VERIFY_DATA_FILES_MANIFEST";

    public DataFilesManifestIntegrityRule() {
        super(KSIE_VERIFY_DATA_FILES_MANIFEST);
    }

    public DataFilesManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_DATA_FILES_MANIFEST);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        RuleResult result = getFailureResult();
        DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
        try {
            DataHash expectedHash = getDataHashFromSignatureManifest(content);
            DataHash realHash = dataFilesManifest.getDataHash(expectedHash.getAlgorithm());
            if (realHash.equals(expectedHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            LOGGER.debug("Verifying datamanifest failed!", e);
        }
        return Arrays.asList(new GenericVerificationResult(result, this, dataFilesManifest));
    }


    private DataHash getDataHashFromSignatureManifest(SignatureContent content) {
        SignatureManifest manifest = content.getSignatureManifest().getRight();
        return manifest.getDataFilesManifestReference().getHash();
    }
}
