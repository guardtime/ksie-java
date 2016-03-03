package com.guardtime.container.annotation;

import com.guardtime.container.AbstractBlockChainContainerTest;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileAnnotationTest extends AbstractBlockChainContainerTest {

    private static final String TEST_DOMAIN = "com.guardtime";

    @Test
    public void testCreateFileAnnotationWithoutInputFile_ThrowNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File must be present");
        new FileAnnotation(null, MIME_TYPE_APPLICATION_TXT, TEST_DOMAIN, ContainerAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutMimeType_ThrowNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new FileAnnotation(new File(TEST_FILE_PATH_TEST_TXT), null, TEST_DOMAIN, ContainerAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutDomain_ThrowNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Domain must be present");
        new FileAnnotation(new File(TEST_FILE_PATH_TEST_TXT), MIME_TYPE_APPLICATION_TXT, null, ContainerAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutAnnotationType_ThrowNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Container type must be present");
        new FileAnnotation(new File(TEST_FILE_PATH_TEST_TXT), MIME_TYPE_APPLICATION_TXT, TEST_DOMAIN, null);
    }

    @Test
    public void testCreateNewFileAnnotation() throws Exception {
        FileAnnotation annotation = new FileAnnotation(loadFile(TEST_FILE_PATH_TEST_TXT), MIME_TYPE_APPLICATION_TXT, TEST_DOMAIN, ContainerAnnotationType.NON_REMOVABLE);
        assertEquals(TEST_DOMAIN, annotation.getDomain());
        assertEquals(ContainerAnnotationType.NON_REMOVABLE, annotation.getAnnotationType());
        assertEquals(MIME_TYPE_APPLICATION_TXT, annotation.getMimeType());
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }

}