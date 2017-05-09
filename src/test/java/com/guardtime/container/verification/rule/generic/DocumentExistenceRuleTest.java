package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DocumentExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new DocumentExistenceRule(defaultRuleStateProvider);

    @Test
    public void testDocumentExistsResultsInOK() throws Exception {
        final FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String documentPath = "somePath";
        when(mockFileReference.getUri()).thenReturn(documentPath);
        when(mockSignatureContent.getDocuments()).thenReturn(Collections.singletonMap(documentPath, Mockito.mock(ContainerDocument.class)));
        when(mockSignatureContent.getDocumentsManifest()).thenReturn(Pair.of("", mockedDocumentsManifest));
        when(mockedDocumentsManifest.getDocumentReferences()).thenAnswer(new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mockFileReference);
            }
        });

        ResultHolder holder = new ResultHolder();
        holder.addResult(mockSignatureContent, new GenericVerificationResult(OK, KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName(), "", documentPath));
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.OK, holder.getAggregatedResult());
    }

    @Test
    public void testDocumentDoesNotExistsResultsInNOK() throws Exception {
        final FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String documentPath = "somePath";
        when(mockFileReference.getUri()).thenReturn(documentPath);
        when(mockSignatureContent.getDocuments()).thenReturn(Collections.<String, ContainerDocument>emptyMap());
        when(mockSignatureContent.getDocumentsManifest()).thenReturn(Pair.of("", mockedDocumentsManifest));
        when(mockedDocumentsManifest.getDocumentReferences()).thenAnswer(new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mockFileReference);
            }
        });

        ResultHolder holder = new ResultHolder();
        holder.addResult(mockSignatureContent, new GenericVerificationResult(OK, KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName(), "", documentPath));
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.NOK, holder.getAggregatedResult());
    }

}