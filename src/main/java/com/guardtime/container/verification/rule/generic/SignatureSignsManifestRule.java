package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;

public class SignatureSignsManifestRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_MANIFEST_HASH.getName();

    public SignatureSignsManifestRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        VerificationResult result = getFailureVerificationResult();
        Manifest manifest = verifiable.getManifest().getRight();
        try {
            DataHash signedHash = verifiable.getContainerSignature().getSignedDataHash();
            DataHash realHash = manifest.getDataHash(signedHash.getAlgorithm());
            if (realHash.equals(signedHash)) {
                result = VerificationResult.OK;
            }
        } catch (DataHashException e) {
            throw new RuleTerminatingException("Failed to verify hash of manifest!", e);
        } finally {
            String manifestUri = verifiable.getManifest().getLeft();
            holder.addResult(new GenericVerificationResult(result, getName(), getErrorMessage(), manifestUri));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Manifest hash differs from the one signed!";
    }
}
