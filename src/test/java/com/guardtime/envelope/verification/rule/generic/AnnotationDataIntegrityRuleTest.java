/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.state.DefaultRuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnnotationDataIntegrityRuleTest {

    private Rule rule = new AnnotationDataIntegrityRule(new DefaultRuleStateProvider());

    @Test
    public void testInvalidNonRemovableAnnotationDataResultsInNOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(false, EnvelopeAnnotationType.NON_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testValidNonRemovableAnnotationDataResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(true, EnvelopeAnnotationType.NON_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testInvalidValueRemovableAnnotationDataResultIsDropped() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(false, EnvelopeAnnotationType.VALUE_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        assertTrue(holder.getResults().size() == 1);
    }

    @Test
    public void testValidValueRemovableAnnotationDataResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(true, EnvelopeAnnotationType.VALUE_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testInvalidFullyRemovableAnnotationDataResultIsDropped() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(false, EnvelopeAnnotationType.FULLY_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        assertTrue(holder.getResults().size() == 1);
    }

    @Test
    public void testValidFullyRemovableAnnotationDataResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(true, EnvelopeAnnotationType.FULLY_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private SignatureContent setUpVerifiable(boolean validAnnotation, EnvelopeAnnotationType annotationType, ResultHolder holder) throws Exception {
        String annotationPath = "annotation.dat";
        String annotationManifestPath = "annotation.ext";
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        EnvelopeAnnotation mockAnnotation = Mockito.mock(EnvelopeAnnotation.class);
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