package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DataFilesManifestIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_DATA_FILES_MANIFEST = "KSIE_VERIFY_DATA_FILES_MANIFEST";

    public DataFilesManifestIntegrityRule() {
        super(KSIE_VERIFY_DATA_FILES_MANIFEST);
    }

    public DataFilesManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_DATA_FILES_MANIFEST);
    }

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
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
            LOGGER.debug("Verifying datamanifest failed!", e);
        }
        results.add(Pair.of(dataFilesManifestReference, new GenericVerificationResult(result, this)));
        return results;
    }


    private DataHash getDataHashFromSignatureManifest(SignatureContent content) {
        SignatureManifest manifest = content.getSignatureManifest().getRight();
        return manifest.getDataFilesManifestReference().getHash();
    }
}
