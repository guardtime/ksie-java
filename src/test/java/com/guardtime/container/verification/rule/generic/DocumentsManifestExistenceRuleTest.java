package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DocumentsManifestExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new DocumentsManifestExistenceRule(defaultRuleStateProvider);

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

        assertEquals(VerificationResult.OK, holder.getAggregatedResult());
    }

    @Test
    public void testDocumentsManifestDoesNotExist_NOK() throws Exception {
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        String documentsManifestPath = "datamanifest.ext";
        when(mockFileReference.getUri()).thenReturn(documentsManifestPath);
        when(mockedManifest.getDocumentsManifestReference()).thenReturn(mockFileReference);
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("path", mockedManifest));
        when(mockSignatureContent.getDocumentsManifest()).thenReturn(null);

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.NOK, holder.getAggregatedResult());
    }

}