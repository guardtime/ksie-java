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

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnnotationDataExistenceRuleTest extends AbstractEnvelopeTest {

    private Rule rule = new AnnotationDataExistenceRule(defaultRuleStateProvider);

    @Test
    public void testNonRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(EnvelopeAnnotationType.NON_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(EnvelopeAnnotationType.VALUE_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationDataExistsResultsInOK() throws Exception {
        ResultHolder holder = new ResultHolder();
        SignatureContent verifiable = setUpVerifiable(EnvelopeAnnotationType.FULLY_REMOVABLE, holder);
        rule.verify(holder, verifiable);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private SignatureContent setUpVerifiable(EnvelopeAnnotationType annotationType, ResultHolder holder) {
        String annotationPath = "annotation.dat";
        String annotationManifestPath = "annotation.ext";
        SignatureContent mockSignatureContent = mock(SignatureContent.class);
        final FileReference mockAnnotationManifestReference = mock(FileReference.class);
        AnnotationDataReference mockAnnotationReference = mock(AnnotationDataReference.class);
        SingleAnnotationManifest mockSignaleAnnotationManifest = mock(SingleAnnotationManifest.class);

        when(mockAnnotationManifestReference.getUri()).thenReturn(annotationManifestPath);
        when(mockAnnotationManifestReference.getMimeType()).thenReturn(annotationType.getContent());
        when(mockAnnotationReference.getUri()).thenReturn(annotationPath);
        when(mockSignaleAnnotationManifest.getAnnotationReference()).thenReturn(mockAnnotationReference);
        when(mockSignatureContent.getAnnotations())
                .thenReturn(Collections.singletonMap(annotationPath, mock(Annotation.class)));
        holder.addResult(
                mockSignatureContent,
                new GenericVerificationResult(
                        VerificationResult.OK,
                        KSIE_VERIFY_ANNOTATION_EXISTS.getName(),
                        "",
                        annotationManifestPath
                )
        );
        when(mockSignatureContent.getSingleAnnotationManifests())
                .thenReturn(Collections.singletonMap(annotationManifestPath, mockSignaleAnnotationManifest));
        AnnotationsManifest mockAnnotationsManifest = mock(AnnotationsManifest.class);
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(mockAnnotationsManifest);
        when(mockAnnotationsManifest.getSingleAnnotationManifestReferences()).thenAnswer(new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mockAnnotationManifestReference);
            }
        });
        return mockSignatureContent;
    }

}