package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.AnnotationsManifest;
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

public class AnnotationsManifestExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new AnnotationsManifestExistenceRule(defaultRuleStateProvider);

    @Test
    public void testAnnotationsManifestDoesExistResultsInOK() throws Exception {
        String annotManifestPath = "annotmanifest.ext";
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);

        when(mockFileReference.getUri()).thenReturn(annotManifestPath);
        when(mockedManifest.getAnnotationsManifestReference()).thenReturn(mockFileReference);
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("path", mockedManifest));
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(Pair.of(annotManifestPath, Mockito.mock(AnnotationsManifest.class)));

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.OK, holder.getAggregatedResult());
    }

    @Test
    public void testAnnotationsManifestDoesNotExistResultsInNOK() throws Exception {
        String annotManifestPath = "annotmanifest.ext";
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);

        when(mockFileReference.getUri()).thenReturn(annotManifestPath);
        when(mockedManifest.getAnnotationsManifestReference()).thenReturn(mockFileReference);
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("path", mockedManifest));
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(null);

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.NOK, holder.getAggregatedResult());
    }

}