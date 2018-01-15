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

package com.guardtime.envelope.extending.ksi;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class PublicationKsiEnvelopeSignatureExtendingPolicyTest extends AbstractEnvelopeTest {

    @Test
    public void testPublicationKsiEnvelopeSignatureExtenderWithoutKsi_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new PublicationKsiEnvelopeSignatureExtendingPolicy(null, mock(PublicationRecord.class));
    }

    @Test
    public void testPublicationKsiEnvelopeSignatureExtenderWithoutPublicationRecord_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Publication record");
        new PublicationKsiEnvelopeSignatureExtendingPolicy(mock(KSI.class), null);
    }

    @Test
    public void testExtendingDelegatesToKsi() throws Exception {
        KSI mockKsi = mock(KSI.class);
        PublicationRecord mockPublicationRecord = mock(PublicationRecord.class);
        when(mockPublicationRecord.getPublicationTime()).thenReturn(new Date(5000));
        ExtendingPolicy<KSISignature> extendingPolicy =
                new PublicationKsiEnvelopeSignatureExtendingPolicy(mockKsi, mockPublicationRecord);
        KSISignature mockSignature = mock(KSISignature.class);
        when(mockSignature.getAggregationTime()).thenReturn(new Date(1000));
        extendingPolicy.getExtendedSignature(mockSignature);
        Mockito.verify(mockKsi, times(1)).extend(any(KSISignature.class), any(PublicationRecord.class));
    }

}
