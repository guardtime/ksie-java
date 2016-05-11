package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
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
 * Rule that verifies the hash integrity of {@link DocumentsManifest} as noted in {@link Manifest}.
 */
public class DocumentsManifestIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_DATA_FILES_MANIFEST = "KSIE_VERIFY_DATA_FILES_MANIFEST";

    public DocumentsManifestIntegrityRule() {
        super(KSIE_VERIFY_DATA_FILES_MANIFEST);
    }

    public DocumentsManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_DATA_FILES_MANIFEST);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        RuleResult result = getFailureResult();
        DocumentsManifest documentsManifest = content.getDocumentsManifest().getRight();
        try {
            DataHash expectedHash = getDataHashFromManifest(content);
            DataHash realHash = documentsManifest.getDataHash(expectedHash.getAlgorithm());
            if (realHash.equals(expectedHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            LOGGER.debug("Verifying datamanifest failed!", e);
        }
        return Arrays.asList(new GenericVerificationResult(result, this, documentsManifest));
    }


    private DataHash getDataHashFromManifest(SignatureContent content) {
        Manifest manifest = content.getManifest().getRight();
        return manifest.getDocumentsManifestReference().getHash();
    }
}
