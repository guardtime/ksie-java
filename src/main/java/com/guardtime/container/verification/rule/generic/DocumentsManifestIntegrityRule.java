package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This rule verifies the validity of the datamanifest which contains records for all {@link
 * com.guardtime.container.document.ContainerDocument}s associated with a signature.
 */
public class DocumentsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    public DocumentsManifestIntegrityRule() {
        this(RuleState.FAIL);
    }

    public DocumentsManifestIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        VerificationResult verificationResult = getFailureVerificationResult();
        DocumentsManifest documentsManifest = verifiable.getDocumentsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        try {
            for(DataHash expectedHash : documentsManifestReference.getHashList()) {
                if(expectedHash.getAlgorithm().getStatus()!= HashAlgorithm.Status.NORMAL) continue; // Skip not implemented or not trusted
                DataHash annotationsManifestHash = documentsManifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(annotationsManifestHash)) {
                    verificationResult = VerificationResult.OK;
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Verifying documents manifest failed!", e);
        }
        TerminatingVerificationResult result = new TerminatingVerificationResult(verificationResult, this, documentsManifestReference.getUri());
        return Arrays.asList((RuleVerificationResult) result);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_DATA_MANIFEST";
    }

    @Override
    public String getErrorMessage() {
        return "Datamanifest hash mismatch.";
    }
}
