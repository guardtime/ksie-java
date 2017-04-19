package com.guardtime.container.verification.rule.generic;

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
import com.guardtime.container.verification.rule.state.DefaultRuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnnotationDataIntegrityRuleTest {

    private Rule rule = new AnnotationDataIntegrityRule(new DefaultRuleStateProvider());

    @Test
    public void testInvalidNonRemovableAnnotationDataResultsInNOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(false, ContainerAnnotationType.NON_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testValidNonRemovableAnnotationDataResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(true, ContainerAnnotationType.NON_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testInvalidValueRemovableAnnotationDataResultIsDropped() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(false, ContainerAnnotationType.VALUE_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        assertTrue(holder.getResults().size() == 1);
    }

    @Test
    public void testValidValueRemovableAnnotationDataResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(true, ContainerAnnotationType.VALUE_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testInvalidFullyRemovableAnnotationDataResultIsDropped() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(false, ContainerAnnotationType.FULLY_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        assertTrue(holder.getResults().size() == 1);
    }

    @Test
    public void testValidFullyRemovableAnnotationDataResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(true, ContainerAnnotationType.FULLY_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private SignatureContent setUpVerifiable(boolean validAnnotation, ContainerAnnotationType annotationType, ResultHolder holder) throws Exception {
        String annotationPath = "annotation.dat";
        String annotationManifestPath = "annotation.ext";
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        ContainerAnnotation mockAnnotation = Mockito.mock(ContainerAnnotation.class);
        final FileReference mockAnnotationManifestReference = Mockito.mock(FileReference.class);
        AnnotationDataReference mockAnnotationReference = Mockito.mock(AnnotationDataReference.class);
        SingleAnnotationManifest mockSignaleAnnotationManifest = Mockito.mock(SingleAnnotationManifest.class);

        when(mockAnnotationManifestReference.getUri()).thenReturn(annotationManifestPath);
        when(mockAnnotationManifestReference.getMimeType()).thenReturn(annotationType.getContent());
        when(mockAnnotationReference.getUri()).thenReturn(annotationPath);
        when(mockAnnotationReference.getHash()).thenReturn(nullDataHash);
        VerificationResult result = VerificationResult.NOK;
        if (validAnnotation) {
            result = VerificationResult.OK;
            when(mockAnnotation.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(nullDataHash);
        }
        holder.addResult(mockSignatureContent, new GenericVerificationResult(result, KSIE_VERIFY_ANNOTATION_EXISTS.getName(), "", annotationManifestPath));
        when(mockSignaleAnnotationManifest.getAnnotationReference()).thenReturn(mockAnnotationReference);
        when(mockSignatureContent.getAnnotations()).thenReturn(Collections.singletonMap(annotationPath, mockAnnotation));
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