package com.guardtime.envelope.annotation;

import com.guardtime.envelope.AbstractEnvelopeTest;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class AnnotationFactoryTest extends AbstractEnvelopeTest {

    @Test
    public void testCreateWithoutParsingStore_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store must be present");
        new AnnotationFactory(null);
    }

    @Test
    public void testCreateCopy_OK() {
        AnnotationFactory factory = new AnnotationFactory(parsingStore);
        Annotation newAnnotation = factory.create(stringEnvelopeAnnotation);
        assertNotSame(stringEnvelopeAnnotation, newAnnotation);
        assertEquals(stringEnvelopeAnnotation, newAnnotation);
    }

    @Test
    public void testCreateFromFile_OK() throws Exception {
        AnnotationFactory factory = new AnnotationFactory(parsingStore);
        Annotation annotation = factory.create(
                loadFile(TEST_FILE_PATH_TEST_TXT), "SomeDomain", EnvelopeAnnotationType.NON_REMOVABLE);
        assertNotNull(annotation);
    }

    @Test
    public void testCreateFromInputStream_OK() {
        AnnotationFactory factory = new AnnotationFactory(parsingStore);
        Annotation annotation = factory.create(
                new ByteArrayInputStream(new byte[32]), "SomeDomain", EnvelopeAnnotationType.NON_REMOVABLE);
        assertNotNull(annotation);
    }

    @Test
    public void testCreateWhereProvidedAnnotationIsInstanceOfFileAnnotation_Ok() throws Exception {
        FileAnnotation annotation = new FileAnnotation(
                loadFile(TEST_FILE_PATH_TEST_TXT), "File.Domain", EnvelopeAnnotationType.NON_REMOVABLE);
        AnnotationFactory factory = new AnnotationFactory(parsingStore);
        Annotation newAnnotation = factory.create(annotation);
        assertNotSame(annotation, newAnnotation);
        assertEquals(annotation, newAnnotation);
    }
}
