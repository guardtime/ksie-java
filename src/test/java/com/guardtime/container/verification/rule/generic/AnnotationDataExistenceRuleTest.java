package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class AnnotationDataExistenceRuleTest extends AbstractContainerTest {

    private Rule rule = new AnnotationDataExistenceRule(defaultRuleStateProvider);

    @Test
    public void testNonRemovableAnnotationDataDoesNotExist_ThrowsRuleTerminatingException_AndResultsInNOK() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("AnnotationData existence could not be verified for");

        ResultHolder holder = new ResultHolder();
        Pair verifiable = setUpVerifiable(false, ContainerAnnotationType.NON_REMOVABLE);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testNonRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        Pair verifiable = setUpVerifiable(true, ContainerAnnotationType.NON_REMOVABLE);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationDataDoesNotExist_ThrowsRuleTerminatingException_AndResultIsDropped() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("AnnotationData existence could not be verified for");

        ResultHolder holder = new ResultHolder();
        Pair verifiable = setUpVerifiable(false, ContainerAnnotationType.VALUE_REMOVABLE);
        rule.verify(holder, verifiable);

        assertTrue(holder.getResults().isEmpty());
    }

    @Test
    public void testValueRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        Pair verifiable = setUpVerifiable(true, ContainerAnnotationType.VALUE_REMOVABLE);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationDataDoesNotExist_ThrowsRuleTerminatingException_AndResultIsDropped() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("AnnotationData existence could not be verified for");

        ResultHolder holder = new ResultHolder();
        Pair verifiable = setUpVerifiable(false, ContainerAnnotationType.FULLY_REMOVABLE);
        rule.verify(holder, verifiable);

        assertTrue(holder.getResults().isEmpty());
    }

    @Test
    public void testFullyRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        Pair verifiable = setUpVerifiable(true, ContainerAnnotationType.FULLY_REMOVABLE);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private Pair<SignatureContent, FileReference> setUpVerifiable(boolean setUpAnnotation, ContainerAnnotationType annotationType) {
        String annotationPath = "annotation.dat";
        String annotationManifestPath = "annotation.ext";
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        FileReference mockAnnotationManifestReference = Mockito.mock(FileReference.class);
        AnnotationDataReference mockAnnotationReference = Mockito.mock(AnnotationDataReference.class);
        SingleAnnotationManifest mockSignaleAnnotationManifest = Mockito.mock(SingleAnnotationManifest.class);

        when(mockAnnotationManifestReference.getUri()).thenReturn(annotationManifestPath);
        when(mockAnnotationManifestReference.getMimeType()).thenReturn(annotationType.getContent());
        when(mockAnnotationReference.getUri()).thenReturn(annotationPath);
        when(mockSignaleAnnotationManifest.getAnnotationReference()).thenReturn(mockAnnotationReference);
        if (setUpAnnotation) {
            when(mockSignatureContent.getAnnotations()).thenReturn(Collections.singletonMap(annotationPath, Mockito.mock(ContainerAnnotation.class)));
        }
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.singletonMap(annotationManifestPath, mockSignaleAnnotationManifest));

        return Pair.of(mockSignatureContent, mockAnnotationManifestReference);
    }

}