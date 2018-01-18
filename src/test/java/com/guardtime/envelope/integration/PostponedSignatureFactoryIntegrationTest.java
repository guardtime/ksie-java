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

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.packaging.zip.ZipEnvelopeWriter;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.postponed.PostponedSignatureFactory;
import com.guardtime.envelope.verification.EnvelopeVerifier;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.policy.DefaultVerificationPolicy;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.signature.ksi.KsiSignatureVerifier;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.verifier.policies.InternalVerificationPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.guardtime.envelope.verification.result.VerificationResult.NOK;
import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

public class PostponedSignatureFactoryIntegrationTest extends AbstractCommonIntegrationTest {
    private EnvelopePackagingFactory postponedPackagingFactory;
    private PostponedSignatureFactory postponedSignatureFactory;
    private Envelope testEnvelope;
    private EnvelopeVerifier verifier;
    private SignatureFactory spySignatureFactory;

    @Before
    public void setUpFactories() throws Exception {
        spySignatureFactory = spy(signatureFactory);
        postponedSignatureFactory = new PostponedSignatureFactory(spySignatureFactory);
        postponedPackagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(postponedSignatureFactory)
                .withVerificationPolicy(null)
                .build();
        testEnvelope = postponedPackagingFactory.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        );

        verifier = new EnvelopeVerifier(
                new DefaultVerificationPolicy(new KsiSignatureVerifier(ksi, new InternalVerificationPolicy()))
        );
    }

    @After
    public void cleanUp() throws Exception {
        testEnvelope.close();
    }

    @Test
    public void testReadPostponedEnvelopeWithNotPostponedFactory() throws Exception {
        expectedException.expect(EnvelopeReadingException.class);
        expectedException.expectMessage("Reading envelope encountered errors!");
        try (
                InputStream inputStream = new FileInputStream(loadFile(ENVELOPE_POSTPONED));
                Envelope ignored = packagingFactory.read(inputStream)
        ) {
            //Empty
        }

    }

    @Test
    public void testReadNotPostponedEnvelopeWithPostponedFactory() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Hash algorithm id '-120' is unknown");
        try (
                InputStream inputStream = new FileInputStream(loadFile(ENVELOPE_WITH_ONE_DOCUMENT));
                Envelope ignored = postponedPackagingFactory.read(inputStream)
        ) {
            //Empty
        }
    }

    @Test
    public void testReadPostponedEnvelope() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Provided SignatureContent does not hold PostponedSignature type of EnvelopeSignature");
        try (
                InputStream inputStream = new FileInputStream(loadFile(ENVELOPE_WITH_ONE_DOCUMENT));
                Envelope envelope = packagingFactory.read(inputStream)
        ) {
            for (SignatureContent content : envelope.getSignatureContents()) {
                postponedSignatureFactory.sign(content);
            }
        }
    }

    @Test
    public void testSigning() throws Exception {
        for (SignatureContent content : testEnvelope.getSignatureContents()) {
            postponedSignatureFactory.sign(content);
        }
        verify(OK);
    }

    @Test
    public void testSigningAndStoring() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ZipEnvelopeWriter().write(testEnvelope, bos);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
            testEnvelope = postponedPackagingFactory.read(bis);
            for (SignatureContent content : testEnvelope.getSignatureContents()) {
                postponedSignatureFactory.sign(content);
            }
            verify(OK);
        }
    }

    @Test
    public void testVerificationWithoutSigning() {
        verify(NOK);
    }

    @Test
    public void testSigningWithInvalidHash_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Provided signatures Data hash does not match!");
        try {
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    DataHash hash = new DataHash(HashAlgorithm.SHA2_256, "16161616161616161616161616161616".getBytes());
                    return signatureFactory.create(hash);
                }
            }).when(spySignatureFactory).create(any(DataHash.class));
            for (SignatureContent content : testEnvelope.getSignatureContents()) {
                postponedSignatureFactory.sign(content);
            }
        } finally {
            Mockito.reset(spySignatureFactory);
        }
    }


    private void verify(VerificationResult expected) {
        VerifiedEnvelope verifiedEnvelope = verifier.verify(testEnvelope);
        assertEquals(expected, verifiedEnvelope.getVerificationResult());
    }
}
