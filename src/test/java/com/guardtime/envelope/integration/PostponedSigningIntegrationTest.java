package com.guardtime.envelope.integration;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
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
import java.util.Collections;

import static com.guardtime.envelope.verification.result.VerificationResult.NOK;
import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

public class PostponedSigningIntegrationTest extends AbstractCommonIntegrationTest {
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
        testEnvelope = postponedPackagingFactory.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_ENVELOPE_ANNOTATION));

        verifier = new EnvelopeVerifier(
                new DefaultVerificationPolicy(new KsiSignatureVerifier(ksi, new InternalVerificationPolicy()))
        );
    }

    @After
    public void cleanUp() throws Exception {
        testEnvelope.close();
    }

    @Test
    public void testSigning_OK() throws Exception {
        for(SignatureContent content : testEnvelope.getSignatureContents()) {
            postponedSignatureFactory.sign(content);
        }
        verify(OK);
    }

    @Test
    public void testSigningAndStoring_OK() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ZipEnvelopeWriter().write(testEnvelope, bos);
        try(ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
            testEnvelope = postponedPackagingFactory.read(bis);
            for (SignatureContent content : testEnvelope.getSignatureContents()) {
                postponedSignatureFactory.sign(content);
            }
            verify(OK);
        }
    }

    @Test
    public void testSigningWithInvalidHash_NOK() throws Exception {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                DataHash hash = new DataHash(HashAlgorithm.SHA2_256, "16161616161616161616161616161616".getBytes());
                return signatureFactory.create(hash);
            }
        }).when(spySignatureFactory).create(any(DataHash.class));
        for(SignatureContent content : testEnvelope.getSignatureContents()) {
            postponedSignatureFactory.sign(content);
        }
        verify(NOK);
        Mockito.reset(spySignatureFactory);
    }


    private void verify(VerificationResult expected) {
        VerifiedEnvelope verifiedEnvelope = verifier.verify(testEnvelope);
        assertEquals(expected, verifiedEnvelope.getVerificationResult());
    }
}
