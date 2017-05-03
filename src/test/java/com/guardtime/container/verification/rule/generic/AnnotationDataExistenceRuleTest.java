package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnnotationDataExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new AnnotationDataExistenceRule(defaultRuleStateProvider);

    @Test
    public void testNonRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(ContainerAnnotationType.NON_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(ContainerAnnotationType.VALUE_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(ContainerAnnotationType.FULLY_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private SignatureContent setUpVerifiable(ContainerAnnotationType annotationType, ResultHolder holder) {
        String annotationPath = "annotation.dat";
        String annotationManifestPath = "annotation.ext";
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        final FileReference mockAnnotationManifestReference = Mockito.mock(FileReference.class);
        AnnotationDataReference mockAnnotationReference = Mockito.mock(AnnotationDataReference.class);
        SingleAnnotationManifest mockSignaleAnnotationManifest = Mockito.mock(SingleAnnotationManifest.class);

        when(mockAnnotationManifestReference.getUri()).thenReturn(annotationManifestPath);
        when(mockAnnotationManifestReference.getMimeType()).thenReturn(annotationType.getContent());
        when(mockAnnotationReference.getUri()).thenReturn(annotationPath);
        when(mockSignaleAnnotationManifest.getAnnotationReference()).thenReturn(mockAnnotationReference);
        when(mockSignatureContent.getAnnotations()).thenReturn(Collections.singletonMap(annotationPath, Mockito.mock(ContainerAnnotation.class)));
        holder.addResult(mockSignatureContent, new GenericVerificationResult(VerificationResult.OK, KSIE_VERIFY_ANNOTATION_EXISTS.getName(), "", annotationManifestPath));
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.singletonMap(annotationManifestPath, mockSignaleAnnotationManifest));
        AnnotationsManifest mockAnnotationsManifest = mock(AnnotationsManifest.class);
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(Pair.of("", mockAnnotationsManifest));
        when(mockAnnotationsManifest.getSingleAnnotationManifestReferences()).thenAnswer(new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mockAnnotationManifestReference);
            }
        });
        return mockSignatureContent;
    }

}