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
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static com.guardtime.envelope.verification.result.VerificationResult.NOK;
import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SingleAnnotationManifestExistenceRuleTest extends AbstractEnvelopeTest {

    private Rule rule = new SingleAnnotationManifestExistenceRule(defaultRuleStateProvider);

    @Test
    public void testManifestExists_OK() throws Exception {
        final FileReference mockFileReference = Mockito.mock(FileReference.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        String manifesturi = "uri";
        when(mockFileReference.getUri()).thenReturn(manifesturi);
        when(mockFileReference.getMimeType()).thenReturn(EnvelopeAnnotationType.NON_REMOVABLE.getContent());
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.singletonMap(manifesturi, mockedSingleAnnotationManifest));
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(mockedAnnotationsManifest);
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
        when(mockFileReference.getMimeType()).thenReturn(EnvelopeAnnotationType.NON_REMOVABLE.getContent());
        when(mockSignatureContent.getSingleAnnotationManifests()).thenReturn(Collections.<String, SingleAnnotationManifest>emptyMap());
        when(mockSignatureContent.getAnnotationsManifest()).thenReturn(mockedAnnotationsManifest);
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