package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SingleHashAlgorithmProviderTest {

    public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA2_256;
    private SingleHashAlgorithmProvider singleHashAlgorithmProvider = new SingleHashAlgorithmProvider(HASH_ALGORITHM);

    @Test
    public void testGetFileReferenceHashAlgorithms() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getFileReferenceHashAlgorithms().get(0));
    }

    @Test
    public void testGetDocumentReferenceHashAlgorithms() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getDocumentReferenceHashAlgorithms().get(0));
    }

    @Test
    public void testGetAnnotationDataReferenceHashAlgorithm() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getAnnotationDataReferenceHashAlgorithm());
    }

    @Test
    public void testGetSigningHashAlgorithm() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getSigningHashAlgorithm());
    }

}