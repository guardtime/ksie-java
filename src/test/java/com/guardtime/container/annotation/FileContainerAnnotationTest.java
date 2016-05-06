package com.guardtime.container.annotation;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileContainerAnnotationTest extends AbstractContainerTest {

    @Test
    public void testCreateFileAnnotationWithoutInputFile_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("File must be present");
        new FileContainerAnnotation(null, ANNOTATION_DOMAIN_COM_GUARDTIME, ContainerAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutDomain_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Domain must be present");
        new FileContainerAnnotation(new File(TEST_FILE_PATH_TEST_TXT), null, ContainerAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutAnnotationType_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Container type must be present");
        new FileContainerAnnotation(new File(TEST_FILE_PATH_TEST_TXT), ANNOTATION_DOMAIN_COM_GUARDTIME, null);
    }

    @Test
    public void testCreateNewFileAnnotation() throws Exception {
        FileContainerAnnotation annotation = new FileContainerAnnotation(loadFile(TEST_FILE_PATH_TEST_TXT), ANNOTATION_DOMAIN_COM_GUARDTIME, ContainerAnnotationType.NON_REMOVABLE);
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, annotation.getDomain());
        assertEquals(ContainerAnnotationType.NON_REMOVABLE, annotation.getAnnotationType());
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }

}