package com.guardtime.container.verification.policy.rule.generic;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.rule.RuleState;
import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DataFilesManifestIntegrityRule implements VerificationRule {
    private final RuleState state;

    public DataFilesManifestIntegrityRule() {
        state = RuleState.FAIL;
    }

    public DataFilesManifestIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public List<VerificationResult> verify(VerificationContext context) {
        BlockChainContainer container = context.getContainer();
        List<VerificationResult> results = new LinkedList<>();
        for (SignatureContent content : container.getSignatureContents()) {
            RuleResult result = RuleResult.OK;
            Pair<String, DataFilesManifest> dataManifest = content.getDataManifest();
            if(dataManifest == null) {
                FileReference dataFilesManifestReference = content.getSignatureManifest().getRight().getDataFilesManifestReference();
                results.add(new GenericVerificationResult(getFailureResult(), this, dataFilesManifestReference));
                continue;
            }
            DataFilesManifest dataFilesManifest = dataManifest.getRight();
            try {
                SignatureManifest manifest = content.getSignatureManifest().getRight();
                DataHash dataFilesManifestHash = manifest.getDataFilesManifestReference().getHash();
                DataHash realHash = Util.hash(dataFilesManifest.getInputStream(), dataFilesManifestHash.getAlgorithm());
                if (!dataFilesManifestHash.equals(realHash)) {
                    result = getFailureResult();
                }
            } catch (IOException e) {
                // log exception?
                result = getFailureResult();
            }
            results.add(new GenericVerificationResult(result, this, dataFilesManifest));
        }
        return results;
    }

    @Override
    public boolean shouldBeIgnored(List<VerificationResult> previousResults) {
        return state == RuleState.IGNORE;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
