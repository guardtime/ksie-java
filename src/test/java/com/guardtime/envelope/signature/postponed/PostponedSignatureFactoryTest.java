package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostponedSignatureFactoryTest extends AbstractEnvelopeTest {

    private PostponedSignatureFactory limitedFactory = new PostponedSignatureFactory(mock(SignatureFactoryType.class));
    private SignatureFactory spySignatureFactory = spy(new KsiSignatureFactory(mock(KSI.class)));
    private PostponedSignatureFactory fullFactory = new PostponedSignatureFactory(spySignatureFactory);

    @Test
    public void testExtending_ThrowsUnsupportedOperationException() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        limitedFactory.extend(mock(EnvelopeSignature.class), mock(ExtendingPolicy.class));
    }

    @Test
    public void testSigning_ThrowsUnsupportedOperationException() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        limitedFactory.sign(mock(SignatureContent.class));
    }

    @Test
    public void testExtending_OK() throws Exception {
        doNothing().when(spySignatureFactory).extend(any(EnvelopeSignature.class), any(ExtendingPolicy.class));
        EnvelopeSignature sampleSignature = spySignatureFactory.create(
                new DataHash(HashAlgorithm.SHA2_256, "32323232323232323232323232323232".getBytes())
        );
        fullFactory.extend(sampleSignature, mock(ExtendingPolicy.class));
        verify(spySignatureFactory, times(1)).extend(any(EnvelopeSignature.class), any(ExtendingPolicy.class));
    }

    @Test
    public void testSigning_OK() throws Exception {
        SignatureContent mockSignatureContent = mock(SignatureContent.class);
        EnvelopeSignature mockEnvelopeSignature = mock(PostponedSignature.class);
        when(mockEnvelopeSignature.getSignedDataHash())
                .thenReturn(new DataHash(HashAlgorithm.SHA2_256, "32323232323232323232323232323232".getBytes()));
        when(mockSignatureContent.getEnvelopeSignature()).thenReturn(mockEnvelopeSignature);
        fullFactory.sign(mockSignatureContent);
        verify(spySignatureFactory, times(1)).create(any(DataHash.class));
    }

    @Test
    public void testCreating_OK() throws Exception {
        DataHash hash = new DataHash(HashAlgorithm.SHA2_256, "32323232323232323232323232323232".getBytes());
        EnvelopeSignature signature = limitedFactory.create(hash);
        assertEquals(hash, signature.getSignedDataHash());
    }

    @Test
    public void testParsing_OK() throws Exception {
        DataHash hash = new DataHash(HashAlgorithm.SHA2_256, "32323232323232323232323232323232".getBytes());
        try(ByteArrayInputStream input = new ByteArrayInputStream(hash.getImprint())) {
            EnvelopeSignature signature = limitedFactory.read(input);
            assertEquals(hash, signature.getSignedDataHash());
        }
    }

}