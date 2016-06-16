package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This rule verifies that the {@link ContainerDocument} being tested has not been corrupted.
 */
public class DocumentIntegrityRule extends AbstractRule<Pair<FileReference, SignatureContent>> implements Rule<Pair<FileReference, SignatureContent>> {

    public DocumentIntegrityRule() {
        this(RuleState.FAIL);
    }

    public DocumentIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<FileReference, SignatureContent> verifiable) {
        RuleVerificationResult verificationResult;
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
            verificationResult = new TerminatingVerificationResult(result, this, documentUri);
        } catch (IOException | DataHashException e) {
            LOGGER.info("Verifying document failed!", e);
            verificationResult = new TerminatingVerificationResult(result, this, documentUri, e);
        }
        return Arrays.asList(verificationResult);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_DATA_HASH";
    }

    @Override
    public String getErrorMessage() {
        return "Data file hash mismatch.";
    }
}
