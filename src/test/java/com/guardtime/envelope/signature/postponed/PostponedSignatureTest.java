package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostponedSignatureTest extends AbstractEnvelopeTest {

    private static final DataHash DATA_HASH = new DataHash(HashAlgorithm.SHA2_256, new byte[HashAlgorithm.SHA2_256.getLength()]);

    @Test
    public void testCreateWithNull_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Data hash must be present");
        new PostponedSignature<>(null);
    }

    @Test
    public void testSignOnce() throws SignatureException {
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(DATA_HASH);
        signature.sign(mockSignature);
    }

    @Test
    public void testSignMultiple() throws SignatureException {
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(DATA_HASH);
        signature.sign(mockSignature);
        try {
            signature.sign(mockSignature);
        } catch (SignatureException e) {
            Assert.assertTrue(e.getMessage().startsWith(
                    "Failed to assign signature to placeholder as it already has a signature!"
            ));
        }
    }

    @Test
    public void testSignWithRandomSignature_ThrowsIllegalArgumentException() throws SignatureException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Provided signatures Data hash does not match!");
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(
                new DataHash(HashAlgorithm.SHA2_512, new byte[HashAlgorithm.SHA2_512.getLength()])
        );
        signature.sign(mockSignature);
    }
}
