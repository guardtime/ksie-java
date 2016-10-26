package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotationType;
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

public class SingleAnnotationManifestExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new SingleAnnotationManifestExistenceRule(defaultRuleStateProvider);

    @Test
    public void testManifestExists_OK() throws Exception {
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String manifesturi = "uri";
        when(mockFileReference.getUri()).thenReturn(manifesturi);
        when(mockFileReference.getMimeType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE.getContent());
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.singletonMap(manifesturi, mockedSingleAnnotationManifest));
        ResultHolder holder = new ResultHolder();
        rule.verify(holder, Pair.of(mockSignatureContent, mockFileReference));
        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testManifestIsMissing_ThrowsRuleTerminatingException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("SingleAnnotationManifest existence could not be verified for");
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        when(mockFileReference.getMimeType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE.getContent());
        ResultHolder holder = new ResultHolder();
        rule.verify(holder, Pair.of(mockSignatureContent, mockFileReference));
    }

}