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

package com.guardtime.envelope.verification;

import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.MimeType;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.ResultHolder;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifiedEnvelopeTest {

    @Test
    public void testWrappedMethodsProvideAccessToOriginals() {
        Envelope originalEnvelope = setUpMockedEnvelope();
        VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(originalEnvelope, new ResultHolder());
        assertEquals(originalEnvelope.getMimeType(), verifiedEnvelope.getMimeType());
        assertEquals(originalEnvelope.getUnknownFiles(), verifiedEnvelope.getUnknownFiles());
    }

    @Test
    public void testSignatureContentsAreWrapped() {
        VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(setUpMockedEnvelope(), new ResultHolder());
        for(SignatureContent content : verifiedEnvelope.getVerifiedSignatureContents()) {
            assertTrue(content instanceof VerifiedSignatureContent);
        }
    }

    private Envelope setUpMockedEnvelope() {
        Envelope mockedEnvelope = mock(Envelope.class);
        when(mockedEnvelope.getMimeType()).thenReturn(mock(MimeType.class));
        when(mockedEnvelope.getUnknownFiles()).thenReturn(Collections.singletonList(mock(UnknownDocument.class)));
        when(mockedEnvelope.getSignatureContents()).thenAnswer(new Answer<List<? extends SignatureContent>>() {
            @Override
            public List<? extends SignatureContent> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mock(SignatureContent.class));
            }
        });
        return mockedEnvelope;
    }

}