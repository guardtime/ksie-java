package com.guardtime.container.util;

import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;

public class DigestOutputStreamTest {

    private static final byte[] TEST_DATA = new byte[2000];

    @Test
    public void testOutputStreamHasher() throws Exception {

        //TODO bad test
        HashAlgorithm algorithm = HashAlgorithm.SHA2_256;
        DigestOutputStream output2;
        try (DigestOutputStream output  = new DigestOutputStream(new ByteArrayOutputStream(), algorithm)) {
            com.guardtime.ksi.util.Util.copyData(new ByteArrayInputStream(TEST_DATA), output);
            output2 = output;
        }

        assertArrayEquals(Util.hash(new ByteArrayInputStream(TEST_DATA), algorithm).getImprint(), output2.getDataHash().getImprint());

    }
}