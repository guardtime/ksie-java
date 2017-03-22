package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;

/**
 * This rule verifies the validity of the datamanifest which contains records for all {@link
 * com.guardtime.container.document.ContainerDocument}s associated with a signature.
 */
public class DocumentsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_DATA_MANIFEST.getName();
    private final MultiHashElementIntegrityRule integrityRule;

    public DocumentsManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        integrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        MultiHashElement documentsManifest = verifiable.getDocumentsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        integrityRule.verify(holder, Pair.of(documentsManifest, documentsManifestReference));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Datamanifest hash mismatch.";
    }

    @Override
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if (result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName())) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }
}
