package com.guardtime.container.verification.policy.rule.generic;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.rule.RuleState;
import com.guardtime.container.verification.policy.rule.SignatureContentRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DataFilesManifestIntegrityRule implements SignatureContentRule {
    private final RuleState state;

    public DataFilesManifestIntegrityRule() {
        state = RuleState.FAIL;
    }

    public DataFilesManifestIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public List<VerificationResult> verify(SignatureContent content, VerificationContext context) {
        List<VerificationResult> results = new LinkedList<>();
        RuleResult result = getFailureResult();
        FileReference dataFilesManifestReference = content.getSignatureManifest().getRight().getDataFilesManifestReference();
        try {
            DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
            DataHash expectedHash = getDataHashFromSignatureManifest(content);
            DataHash realHash = Util.hash(dataFilesManifest.getInputStream(), expectedHash.getAlgorithm());
            if (expectedHash.equals(realHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            // TODO: log exception?
        }
        results.add(new GenericVerificationResult(result, this, dataFilesManifestReference));
        return results;
    }

    private DataHash getDataHashFromSignatureManifest(SignatureContent content) {
        SignatureManifest manifest = content.getSignatureManifest().getRight();
        return manifest.getDataFilesManifestReference().getHash();
    }

    @Override
    public boolean shouldBeIgnored(SignatureContent content, VerificationContext context) {
        return state == RuleState.IGNORE;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_DATA_FILES_MANIFEST";
    }

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
