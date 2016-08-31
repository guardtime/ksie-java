package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleStateProvider;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;

/**
 * This rule verifies that the {@link ContainerDocument} being tested has not been corrupted.
 */
public class DocumentIntegrityRule extends AbstractRule<Pair<FileReference, SignatureContent>> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_HASH.name();

    public DocumentIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<FileReference, SignatureContent> verifiable) {
        VerificationResult result = getFailureVerificationResult();
        String documentUri = verifiable.getLeft().getUri();
        try {
            ContainerDocument document = verifiable.getRight().getDocuments().get(documentUri);
            for (DataHash expectedHash : verifiable.getLeft().getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() != HashAlgorithm.Status.NORMAL) {
                    continue; // Skip not implemented or not trusted
                }
                DataHash realHash = document.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(realHash)) {
                    result = VerificationResult.OK;
                }
            }
            holder.addResult(new GenericVerificationResult(result, this, documentUri));
        } catch (IOException | DataHashException e) {
            LOGGER.info("Verifying document failed!", e);
            holder.addResult(new GenericVerificationResult(result, this, documentUri, e));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Data file hash mismatch.";
    }
}
