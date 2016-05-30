package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

public class DocumentsIntegrityRule extends AbstractRule<SignatureContent>{

    public DocumentsIntegrityRule() {
        this(RuleState.FAIL);
    }

    public DocumentsIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    public List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        List<RuleVerificationResult> results = new LinkedList<>();
        results.addAll(new DocumentsManifestIntegrityRule(state).verify(verifiable));
        if(terminateVerification(results)) return results;
        DocumentsManifest documentsManifest = verifiable.getDocumentsManifest().getRight();
        List<? extends FileReference> documentsReferences = documentsManifest.getDocumentReferences();
        for(FileReference reference : documentsReferences) {
            List<RuleVerificationResult> existenceResults = new DocumentExistenceRule(state).verify(Pair.of(reference, verifiable));
            results.addAll(existenceResults);
            if(terminateVerification(existenceResults)) continue;
            results.addAll(new DocumentIntegrityRule(state).verify(Pair.of(reference, verifiable)));
        }

        return results;
    }
}
