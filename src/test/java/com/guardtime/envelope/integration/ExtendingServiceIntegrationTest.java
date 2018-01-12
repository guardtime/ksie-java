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

package com.guardtime.envelope.integration;

import com.guardtime.envelope.extending.EnvelopeSignatureExtender;
import com.guardtime.envelope.extending.ExtendedEnvelope;
import com.guardtime.envelope.extending.ExtendedSignatureContent;
import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.envelope.extending.ksi.KsiEnvelopeSignatureExtendingPolicy;
import com.guardtime.envelope.extending.ksi.PublicationKsiEnvelopeSignatureExtendingPolicy;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.publication.inmemory.PublicationsFilePublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.inmemory.LegacyIdentity;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtendingServiceIntegrationTest extends AbstractCommonIntegrationTest {

    @Test
    public void testVerifyOriginalEnvelopeIsExtended() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MULTIPLE_SIGNATURES)){
            KsiEnvelopeSignatureExtendingPolicy policy = new KsiEnvelopeSignatureExtendingPolicy(ksi);
            EnvelopeSignatureExtender extender = new EnvelopeSignatureExtender(new KsiSignatureFactory(ksi), policy);
            extender.extend(envelope);
            ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(envelope);
            assertTrue(extendedEnvelope.isFullyExtended());
        }
    }

    @Test
    public void testExtendingEnvelopeWithValidAndInvalidSignatures()throws Exception {
        ExtendingPolicy policy = new KsiEnvelopeSignatureExtendingPolicy(ksi);
        EnvelopeSignatureExtender extender = new EnvelopeSignatureExtender(signatureFactory, policy);
        try (Envelope envelope = getEnvelope(ENVELOPE_WITH_MULTI_CONTENT_ONE_SIGNATURE_IS_INVALID)) {
            assertSignaturesExtendedStatus(envelope, false);
            ExtendedEnvelope extendedEnvelope = extender.extend(envelope);
            for (ExtendedSignatureContent content : extendedEnvelope.getExtendedSignatureContents()) {
                String uri = content.getManifest().getSignatureReference().getUri();
                if (uri.equals("META-INF/signature-1.ksi")) {
                    assertEquals(true, content.isExtended());
                } else if (uri.equals("META-INF/signature-01-02-03-04-05.ksi")) {
                    assertEquals(false, content.isExtended());
                }
            }
        }
    }

    @Test
    public void testExtendingWithKsiEnvelopeSignatureExtender() throws Exception {
        ExtendingPolicy policy = new KsiEnvelopeSignatureExtendingPolicy(ksi);
        doExtendingTest(signatureFactory, policy);
    }

    @Test
    public void testExtendingWithPublicationKsiEnvelopeSignatureExtender() throws Exception {
        PublicationData publicationData = new PublicationData(
                //June 2016 publication string
                "AAAAAA-CXMCNI-AAJIV3-RB5OEJ-JBK57H-SJ42PI-IB2RE7-2CA2TM-H5W3EF-TF2BX7-HRNRP5-Q2E754"
        );
        PublicationsFilePublicationRecord publicationRecord = new PublicationsFilePublicationRecord(publicationData);
        ExtendingPolicy policy = new PublicationKsiEnvelopeSignatureExtendingPolicy(ksi, publicationRecord);
        doExtendingTest(signatureFactory, policy);
    }

    @Test
    public void testExtendingWithPublicationKsiEnvelopeSignatureExtender_WithOlderPublicationString() throws Exception {
        PublicationData publicationData = new PublicationData(
                //April 2015 publication string
                "AAAAAA-CVFWVA-AAPV2S-SN3JLW-YEKPW3-AUSQP6-PF65K5-KVGZZA-7UYTOV-27VX54-VVJQFG-VCK6GR"
        );
        PublicationsFilePublicationRecord publicationRecord = new PublicationsFilePublicationRecord(publicationData);
        ExtendingPolicy policy = new PublicationKsiEnvelopeSignatureExtendingPolicy(ksi, publicationRecord);
        doExtendingTest(ENVELOPE_WITH_MULTIPLE_EXTENDABLE_SIGNATURES, signatureFactory, policy, false);
    }

    @Test
    public void testExtendingWithInvalidSignature() throws Exception {
        ExtendingPolicy policy = mock(ExtendingPolicy.class);
        when(policy.getExtendedSignature(Mockito.any(Object.class))).thenReturn(mock(KSISignature.class));
        doExtendingTest(ENVELOPE_WITH_ONE_DOCUMENT, signatureFactory, policy, false);
    }

    @Test
    public void testExtendingWithNotExtendedSignature_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(ENVELOPE_WITH_ONE_DOCUMENT);
        when(mockedSignature.isExtended()).thenReturn(false);
        doExtendingTest(ENVELOPE_WITH_ONE_DOCUMENT, signatureFactory, mockedSignature);
    }

    @Test
    public void testExtendingWithDifferentInputHash_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(ENVELOPE_WITH_ONE_DOCUMENT);
        when(mockedSignature.getInputHash()).thenReturn(new DataHash(HashAlgorithm.SHA2_512, new byte[64]));
        doExtendingTest(ENVELOPE_WITH_ONE_DOCUMENT, signatureFactory, mockedSignature);
    }

    @Test
    public void testExtendingWithDifferentAggregationTime_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(ENVELOPE_WITH_ONE_DOCUMENT);
        when(mockedSignature.getAggregationTime()).thenReturn(new Date());
        doExtendingTest(ENVELOPE_WITH_ONE_DOCUMENT, signatureFactory, mockedSignature);
    }

    @Test
    public void testExtendingWithDifferentIdentity_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(ENVELOPE_WITH_ONE_DOCUMENT);
        when(mockedSignature.getAggregationHashChainIdentity())
                .thenReturn(new LegacyIdentity[]{new LegacyIdentity("Invalid identity")});
        doExtendingTest(ENVELOPE_WITH_ONE_DOCUMENT, signatureFactory, mockedSignature);
    }

    private void doExtendingTest(String envelopeName, SignatureFactory factory, KSISignature signature) throws Exception {
        ExtendingPolicy mockedPolicy = mock(ExtendingPolicy.class);
        when(mockedPolicy.getExtendedSignature(Mockito.any(Object.class))).thenReturn(signature);
        doExtendingTest(envelopeName, factory, mockedPolicy, false);
    }

    private void doExtendingTest(SignatureFactory factory, ExtendingPolicy policy) throws Exception {
        doExtendingTest(ENVELOPE_WITH_MULTIPLE_EXTENDABLE_SIGNATURES, factory, policy, true);
    }

    private void doExtendingTest(String envelopeName, SignatureFactory factory, ExtendingPolicy policy,
                                 boolean extendedStatusAfterExtending) throws Exception {
        EnvelopeSignatureExtender extender = new EnvelopeSignatureExtender(factory, policy);
        try (Envelope envelope = getEnvelope(envelopeName)) {
            assertSignaturesExtendedStatus(envelope, false);
            ExtendedEnvelope extendedEnvelope = extender.extend(envelope);
            assertSignaturesExtendedStatus(extendedEnvelope, extendedStatusAfterExtending);
            assertEquals(extendedStatusAfterExtending, extendedEnvelope.isFullyExtended());
        }
    }

    private void assertSignaturesExtendedStatus(Envelope envelope, boolean status) {
        for (SignatureContent content : envelope.getSignatureContents()) {
            assertNotNull(content.getEnvelopeSignature());
            assertNotNull(content.getEnvelopeSignature().getSignature());
            assertEquals(status, content.getEnvelopeSignature().isExtended());
        }
    }

    private KSISignature getMockedSignature(String envelopeName) throws Exception {
        try (Envelope envelope = getEnvelope(envelopeName)) {
            KSISignature signature = (KSISignature) envelope.getSignatureContents().get(0).getEnvelopeSignature().getSignature();

            KSISignature mockedSignature = mock(KSISignature.class);
            when(mockedSignature.getAggregationTime()).thenReturn(signature.getAggregationTime());
            when(mockedSignature.getAggregationHashChainIdentity()).thenReturn(signature.getAggregationHashChainIdentity());
            when(mockedSignature.isExtended()).thenReturn(true);
            when(mockedSignature.getInputHash()).thenReturn(signature.getInputHash());
            return mockedSignature;
        }
    }
}
