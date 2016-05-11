package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Rule that verifies the hash integrity of each {@link ContainerDocument} in {@link SignatureContent} as noted by
 * {@link DocumentsManifest}.
 */
public class DocumentIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_DATA_FILE = "KSIE_VERIFY_DATA_FILE";

    public DocumentIntegrityRule() {
        super(KSIE_VERIFY_DATA_FILE);
    }

    public DocumentIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_DATA_FILE);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<GenericVerificationResult> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        DocumentsManifest documentsManifest = content.getDocumentsManifest().getRight();
        for (FileReference reference : documentsManifest.getDocumentReferences()) {
            results.add(verifyReference(content, reference));
        }
        return results;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        DocumentsManifest documentsManifest = content.getDocumentsManifest().getRight();
        for (RuleVerificationResult result : context.getResultsFor(documentsManifest)) {
            if (!RuleResult.OK.equals(result)) return true;
        }
        return false;
    }

    private GenericVerificationResult verifyReference(SignatureContent content, FileReference reference) {
        RuleResult result = getFailureResult();
        ContainerDocument document = getDocumentForReference(reference, content);
        try {
            DataHash expectedHash = reference.getHash();
            DataHash realHash = document.getDataHash(expectedHash.getAlgorithm());
            if (realHash.equals(expectedHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException | DataHashException e) {
            LOGGER.debug("Verifying data file failed!", e);
        }
        return new GenericVerificationResult(result, this, document);
    }

    private ContainerDocument getDocumentForReference(FileReference reference, SignatureContent content) {
        ContainerDocument document = content.getDocuments().get(reference.getUri());
        if (document != null && document.isWritable()) {
            return document;
        }
        return null;
    }
}
