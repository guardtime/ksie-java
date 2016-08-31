package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.*;
import com.guardtime.container.verification.rule.*;
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

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_MANIFEST.name();

    public DocumentsManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        VerificationResult verificationResult = getFailureVerificationResult();
        DocumentsManifest documentsManifest = verifiable.getDocumentsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        String testedElement = documentsManifestReference.getUri();
        try {
            for (DataHash expectedHash : documentsManifestReference.getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() != HashAlgorithm.Status.NORMAL) {
                    continue; // Skip not implemented or not trusted
                }
                DataHash annotationsManifestHash = documentsManifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(annotationsManifestHash)) {
                    verificationResult = VerificationResult.OK;
                }
            }
            holder.addResult(new GenericVerificationResult(verificationResult, this, testedElement));
        } catch (IOException e) {
            LOGGER.info("Verifying documents manifest failed!", e);
            holder.addResult(new GenericVerificationResult(verificationResult, this, testedElement, e));
        }

        if (!verificationResult.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("DocumentsManifest integrity could not be verified for '" + testedElement + "'");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Datamanifest hash mismatch.";
    }
}
