package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;

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
        DocumentsManifest documentsManifest = verifiable.getDocumentsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        return getFileReferenceHashListVerificationResult(documentsManifest, documentsManifestReference);
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
