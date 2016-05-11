package com.guardtime.container.signature.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class KsiSignatureFactoryTest extends AbstractContainerTest{

    @Mock
    private KSI mockKsi;

    @Mock
    private KSISignature mockSignature;

    @Before
    public void setUpMockKsi() throws KSIException {
        when(mockKsi.sign(Mockito.any(DataHash.class))).thenReturn(mockSignature);
        when(mockKsi.read(Mockito.any(byte[].class))).thenReturn(mockSignature);
    }

    @Test
    public void testCreateFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI must be present");
        SignatureFactory signatureFactory = new KsiSignatureFactory(null);
    }

    @Test
    public void testCreateFactory_OK() throws Exception {
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        assertNotNull(signatureFactory);
    }

    @Test
    public void testCreate() throws Exception {
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        DataHash testHash = new DataHash(HashAlgorithm.SHA2_256, "TestStringTestingStuffLongString".getBytes());
        ContainerSignature signature = signatureFactory.create(testHash);
        assertNotNull(signature);
    }

    @Test
    public void testRead() throws Exception {
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        ContainerSignature signature = signatureFactory.read(new ByteArrayInputStream("".getBytes()));
        assertNotNull(signature);
    }
}