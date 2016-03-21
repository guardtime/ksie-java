package com.guardtime.container.verification.policy.rule.generic;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
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

public class DataFileIntegrityRule implements SignatureContentRule {

    private final RuleState state;

    public DataFileIntegrityRule() {
        state = RuleState.FAIL;
    }

    public DataFileIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    @Override
    public String getName() {
        return null; // TODO: Look into option of having nested rules or nested policies inside rules or sth and how the naming of such rules should be handled.
    }

    @Override
    public boolean shouldBeIgnored(SignatureContent content, VerificationContext context) {
        if (state == RuleState.IGNORE) return true;

        Pair<String, DataFilesManifest> dataManifest = content.getDataManifest();
        if (dataManifest == null) return true;
        DataFilesManifest dataFilesManifest = dataManifest.getRight();
        for (VerificationResult result : context.getResults()) {
            if (result.getTested().equals(dataFilesManifest)) {
                return result.getResult() == RuleResult.NOK;
            }
        }
        return false;
    }

    @Override
    public List<VerificationResult> verify(SignatureContent content, VerificationContext context) {
        List<VerificationResult> results = new LinkedList<>();
        DataFilesManifest dataFilesManifest = content.getDataManifest().getRight();
        for (FileReference reference : dataFilesManifest.getDataFileReferences()) {
            RuleResult result = verifyReference(content, reference);
            results.add(new GenericVerificationResult(result, this, reference));
        }
        return results;
    }

    private RuleResult verifyReference(SignatureContent content, FileReference reference) {
        try {
            ContainerDocument document = getDocumentForReference(reference, content);
            DataHash referenceHash = reference.getHash();
            if (document != null) {
                if(document instanceof EmptyContainerDocument || document.getDataHash(referenceHash.getAlgorithm()).equals(referenceHash)) {
                    return RuleResult.OK;
                }
            }
        } catch (IOException | DataHashException e) {
            // TODO: log exception?
        }
        return getFailureResult();
    }

    private ContainerDocument getDocumentForReference(FileReference reference, SignatureContent content) {
        for (ContainerDocument document : content.getDocuments()) {
            if (reference.getUri().equals(document.getFileName())) {
                return document;
            }
        }
        return null;
    }

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
