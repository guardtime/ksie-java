package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.Arrays;
import java.util.List;

/**
 * This rule verifies that the documents manifest is actually present in the {@link com.guardtime.container.packaging.Container}
 */
public class DocumentsManifestExistenceRule extends AbstractRule<SignatureContent> {

    public DocumentsManifestExistenceRule() {
        this(RuleState.FAIL);
    }

    public DocumentsManifestExistenceRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {

        VerificationResult verificationResult = getFailureVerificationResult();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        Pair<String, DocumentsManifest> documentsManifest = verifiable.getDocumentsManifest();
        if (documentsManifest != null) {
            verificationResult = VerificationResult.OK;
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
        return "Datamanifest is not present in the container.";
    }
}
