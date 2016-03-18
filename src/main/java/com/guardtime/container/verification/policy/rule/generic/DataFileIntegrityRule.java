package com.guardtime.container.verification.policy.rule.generic;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.rule.RuleState;
import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DataFileIntegrityRule implements VerificationRule {

    private final RuleState state;

    public DataFileIntegrityRule() {
        state = RuleState.FAIL;
    }

    public DataFileIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public boolean shouldBeIgnored(List<VerificationResult> previousResults) {
        return state == RuleState.IGNORE;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    @Override
    public List<VerificationResult> verify(VerificationContext context) {
        BlockChainContainer container = context.getContainer();
        List<VerificationResult> results = new LinkedList<>();
        for (SignatureContent content : container.getSignatureContents()) {
            if (skipSignatureContent(content, context)) continue;
            results.addAll(verifySignatureContent(content, context));
        }
        return results;
    }

    private boolean skipSignatureContent(SignatureContent content, VerificationContext context) {
        Pair<String, DataFilesManifest> dataManifest = content.getDataManifest();
        if(dataManifest == null) return true;
        DataFilesManifest dataFilesManifest = dataManifest.getRight();
        for (VerificationResult result : context.getResults()) {
            if (result.getTested().equals(dataFilesManifest)) {
                return result.getResult() == RuleResult.NOK;
            }
        }
        return false;
    }

    private Collection<? extends VerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
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
            if (document == null
                    || document instanceof EmptyContainerDocument
                    || !document.getDataHash(referenceHash.getAlgorithm()).equals(referenceHash)) {
                return getFailureResult();
            }
        } catch (IOException | DataHashException e) {
            // log exception?
            return getFailureResult();
        }
        return RuleResult.OK;
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
