package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.RuleStateProvider;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import java.util.List;

/**
 * This is a delegating rule, not verifying directly but by calling relevant rules to verify sub-components. This rule
 * handles verifying datamanifest and {@link com.guardtime.container.document.ContainerDocument}s referred to by the
 * datamanifest.
 */
public class DocumentsIntegrityRule extends AbstractRule<SignatureContent> {

    private DocumentsManifestExistenceRule documentsManifestExistenceRule;
    private DocumentsManifestIntegrityRule documentsManifestIntegrityRule;
    private DocumentExistenceRule documentExistenceRule;
    private DocumentIntegrityRule documentIntegrityRule;

    public DocumentsIntegrityRule(RuleStateProvider stateProvider) {
        super(RuleState.FAIL);
        documentsManifestExistenceRule = new DocumentsManifestExistenceRule(stateProvider);
        documentsManifestIntegrityRule = new DocumentsManifestIntegrityRule(stateProvider);
        documentExistenceRule = new DocumentExistenceRule(stateProvider);
        documentIntegrityRule = new DocumentIntegrityRule(stateProvider);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) {

        if (!processDocumentsManifestVerification(holder, verifiable)) return;
        //Documents
        for (FileReference reference : getDocumentsFileReferences(verifiable)) {
            processDocumentVerification(holder, Pair.of(reference, verifiable));
        }
    }

    private boolean processDocumentsManifestVerification(ResultHolder holder, SignatureContent verifiable) {
        try {
            documentsManifestExistenceRule.verify(holder, verifiable);
            documentsManifestIntegrityRule.verify(holder, verifiable);
        } catch (RuleTerminatingException e) {
            LOGGER.info("Halting DocumentsManifest verification chain! Caused by '{}'", e.getMessage());
            return false;
        }
        return true;
    }

    private void processDocumentVerification(ResultHolder holder, Pair<FileReference, SignatureContent> verifiable) {
        try {
            documentExistenceRule.verify(holder, verifiable);
            documentIntegrityRule.verify(holder, verifiable);
        } catch (RuleTerminatingException e) {
            LOGGER.info("Halting Document verification chain! Caused by '{}'", e.getMessage());
        }
    }

    private List<? extends FileReference> getDocumentsFileReferences(SignatureContent verifiable) {
        DocumentsManifest documentsManifest = verifiable.getDocumentsManifest().getRight();
        return documentsManifest.getDocumentReferences();
    }
}
