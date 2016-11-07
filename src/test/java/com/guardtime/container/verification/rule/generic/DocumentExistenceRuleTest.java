package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.document.ContainerDocument;
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

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class DocumentExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new DocumentExistenceRule(defaultRuleStateProvider);

    @Test
    public void testDocumentDoesNotExist_ThrowsRuleTerminatingException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Document existence could not be verified for");
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        when(mockFileReference.getUri()).thenReturn("somePath");
        rule.verify(new ResultHolder(), Pair.of(mockFileReference, mockSignatureContent));
    }

    @Test
    public void testDocumentExistsResultsInOK() throws Exception {
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String documentPath = "somePath";
        when(mockFileReference.getUri()).thenReturn(documentPath);
        when(mockSignatureContent.getDocuments()).thenReturn(Collections.singletonMap(documentPath, Mockito.mock(ContainerDocument.class)));

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, Pair.of(mockFileReference, mockSignatureContent));

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

}