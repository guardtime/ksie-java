package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DataFileIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_DATA_FILE = "KSIE_VERIFY_DATA_FILE";

    public DataFileIntegrityRule() {
        super(KSIE_VERIFY_DATA_FILE);
    }

    public DataFileIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_DATA_FILE);
    }

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;
        DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
        for (FileReference reference : dataFilesManifest.getDataFileReferences()) {
            RuleResult result = verifyReference(content, reference);
            results.add(Pair.of(reference, new GenericVerificationResult(result, this)));
        }
        return results;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference dataFilesManifestReference = signatureManifest.getDataFilesManifestReference();
        for (RuleVerificationResult result : context.getResultsFor(dataFilesManifestReference)) {
            if (!RuleResult.OK.equals(result)) return true;
        }
        return false;
    }

    private RuleResult verifyReference(SignatureContent content, FileReference reference) {
        try {
            ContainerDocument document = getDocumentForReference(reference, content);
            DataHash referenceHash = reference.getHash();
            if (document.getDataHash(referenceHash.getAlgorithm()).equals(referenceHash)) {
                return RuleResult.OK;
            }
        } catch (NullPointerException | IOException | DataHashException e) {
            // TODO: log exception?
        }
        return getFailureResult();
    }

    private ContainerDocument getDocumentForReference(FileReference reference, SignatureContent content) {
        for (ContainerDocument document : content.getDocuments()) {
            if (reference.getUri().equals(document.getFileName())) {
                if (document.isWritable()) {
                    return document;
                }
                break;
            }
        }
        return null;
    }
}
