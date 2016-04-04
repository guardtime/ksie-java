package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
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

public class DataFileIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_DATA_FILE = "KSIE_VERIFY_DATA_FILE";

    public DataFileIntegrityRule() {
        super(KSIE_VERIFY_DATA_FILE);
    }

    public DataFileIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_DATA_FILE);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<GenericVerificationResult> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
        for (FileReference reference : dataFilesManifest.getDataFileReferences()) {
            results.add(verifyReference(content, reference));
        }
        return results;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
        for (RuleVerificationResult result : context.getResultsFor(dataFilesManifest)) {
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
