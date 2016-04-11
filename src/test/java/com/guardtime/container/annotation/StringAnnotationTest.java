package com.guardtime.container.annotation;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringAnnotationTest extends AbstractContainerTest {


    @Test
    public void testCreateStringAnnotationWithoutInputString_ThrowNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Content must be present");
        new StringAnnotation(ContainerAnnotationType.NON_REMOVABLE, null, ANNOTATION_DOMAIN_COM_GUARDTIME);
    }

    @Test
    public void testCreateStringAnnotationWithoutAnnotationType_ThrowNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Type must be present");
        new StringAnnotation(null, "Example Content", ANNOTATION_DOMAIN_COM_GUARDTIME);
    }

    @Test
    public void testCreateNewFileAnnotation() throws Exception {
        StringAnnotation annotation = new StringAnnotation(ContainerAnnotationType.NON_REMOVABLE, "Example Content", ANNOTATION_DOMAIN_COM_GUARDTIME);
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, annotation.getDomain());
        assertEquals(ContainerAnnotationType.NON_REMOVABLE, annotation.getAnnotationType());
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }


}