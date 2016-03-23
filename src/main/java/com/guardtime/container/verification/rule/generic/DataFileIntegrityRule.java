package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
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
        this(RuleState.FAIL);
    }

    public DataFileIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<RuleVerificationResult> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;
        DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
        for (FileReference reference : dataFilesManifest.getDataFileReferences()) {
            RuleResult result = verifyReference(content, reference);
            results.add(new GenericVerificationResult(result, KSIE_VERIFY_DATA_FILE, reference));
        }
        return results;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        Pair<String, DataFilesManifest> dataManifest = content.getDataManifest();
        if (dataManifest == null) return true;
        DataFilesManifest dataFilesManifest = dataManifest.getRight();
        for (RuleVerificationResult result : context.getResults()) {
            if (result.getTested().equals(dataFilesManifest)) {
                return RuleResult.NOK.equals(result.getResult());
            }
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
                if(document.isWritable()) {
                    return document;
                }
                break;
            }
        }
        return null;
    }
}
