package com.guardtime.container.annotation;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringContainerAnnotationTest extends AbstractContainerTest {


    @Test
    public void testCreateStringAnnotationWithoutInputString_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Content must be present");
        new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, null, ANNOTATION_DOMAIN_COM_GUARDTIME);
    }

    @Test
    public void testCreateStringAnnotationWithoutAnnotationType_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Type must be present");
        new StringContainerAnnotation(null, "Example Content", ANNOTATION_DOMAIN_COM_GUARDTIME);
    }

    @Test
    public void testCreateNewFileAnnotation() throws Exception {
        StringContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, "Example Content", ANNOTATION_DOMAIN_COM_GUARDTIME);
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, annotation.getDomain());
        assertEquals(ContainerAnnotationType.NON_REMOVABLE, annotation.getAnnotationType());
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }


}