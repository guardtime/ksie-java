package com.guardtime.container.annotation;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MissingAnnotationTest {

    private static final String TEST_DOMAIN = "com.guardtime.test";
    ContainerAnnotation annotation;

    @Before
    public void setUp() throws Exception {
        DataHash hash = Util.hash(new ByteArrayInputStream("".getBytes()), HashAlgorithm.SHA2_256);
        annotation = new MissingAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, TEST_DOMAIN, hash);
    }

    @Test
    public void testGetAnnotationType() throws Exception {
        assertNotNull(annotation.getAnnotationType());
    }

    @Test
    public void testGetDomain() throws Exception {
        assertNotNull(annotation.getDomain());
    }

    @Test
    public void testGetInputStream() throws Exception {
        assertNull(annotation.getInputStream());
    }

    @Test
    public void testGetDataHash() throws Exception {
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }
}