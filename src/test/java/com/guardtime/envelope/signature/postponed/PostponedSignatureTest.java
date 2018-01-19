package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostponedSignatureTest extends AbstractEnvelopeTest {

    public static final DataHash DATA_HASH = new DataHash(HashAlgorithm.SHA2_256, "32323232323232323232323232323232".getBytes());

    @Test
    public void testCreateWithNull_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Data hash must be present");
        new PostponedSignature<>(null);
    }

    @Test
    public void testSignOnce() {
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(DATA_HASH);
        assertTrue(signature.sign(mockSignature));
    }

    @Test
    public void testSignMultiple() {
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(DATA_HASH);
        assertTrue(signature.sign(mockSignature));
        assertFalse(signature.sign(mockSignature));
    }

    @Test
    public void testSignWithRandomSignature_ThrowsIllegalArgumentException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Provided signatures Data hash does not match!");
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(
                new DataHash(HashAlgorithm.SHA2_256, "00000000000000000000000000000000".getBytes())
        );
        signature.sign(mockSignature);
    }

}
