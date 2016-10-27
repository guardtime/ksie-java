package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class DocumentsManifestExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new DocumentsManifestExistenceRule(defaultRuleStateProvider);

    @Test
    public void testDocumentsManifestDoesNotExist_ThrowsRuleTerminatingException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("DocumentsManifest existence could not be verified for");
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        when(mockedManifest.getDocumentsManifestReference()).thenReturn(Mockito.mock(FileReference.class));
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("path", mockedManifest));
        rule.verify(new ResultHolder(), mockSignatureContent);
    }

    @Test
    public void testDocumentsManifestExistsResultsInOK() throws Exception {
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        String documentsManifestPath = "datamanifest.ext";
        when(mockFileReference.getUri()).thenReturn(documentsManifestPath);
        when(mockedManifest.getDocumentsManifestReference()).thenReturn(mockFileReference);
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("path", mockedManifest));
        when(mockSignatureContent.getDocumentsManifest()).thenReturn(Pair.of(documentsManifestPath, mockedDocumentsManifest));

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

}