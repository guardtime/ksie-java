package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.container.verification.result.VerificationResult.NOK;
import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SingleAnnotationManifestExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new SingleAnnotationManifestExistenceRule(defaultRuleStateProvider);

    @Test
    public void testManifestExists_OK() throws Exception {
        final FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String manifesturi = "uri";
        when(mockFileReference.getUri()).thenReturn(manifesturi);
        when(mockFileReference.getMimeType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE.getContent());
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.singletonMap(manifesturi, mockedSingleAnnotationManifest));
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(Pair.of("", mockedAnnotationsManifest));
        when(mockedAnnotationsManifest.getSingleAnnotationManifestReferences()).thenAnswer(new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mockFileReference);
            }
        });
        ResultHolder holder = new ResultHolder();
        holder.addResult(mockSignatureContent, new GenericVerificationResult(OK, KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName(), "", manifesturi));
        rule.verify(holder, mockSignatureContent);
        assertEquals(OK, holder.getAggregatedResult());
    }

    @Test
    public void testManifestIsMissing_NOK() throws Exception {
        final FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String manifesturi = "uri";
        when(mockFileReference.getUri()).thenReturn(manifesturi);
        when(mockFileReference.getMimeType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE.getContent());
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.<String, SingleAnnotationManifest>emptyMap());
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(Pair.of("", mockedAnnotationsManifest));
        when(mockedAnnotationsManifest.getSingleAnnotationManifestReferences()).thenAnswer(new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mockFileReference);
            }
        });
        ResultHolder holder = new ResultHolder();
        holder.addResult(mockSignatureContent, new GenericVerificationResult(OK, KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName(), "", manifesturi));
        rule.verify(holder, mockSignatureContent);
        assertEquals(NOK, holder.getAggregatedResult());
    }

}