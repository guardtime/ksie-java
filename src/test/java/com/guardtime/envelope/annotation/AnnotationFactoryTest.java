package com.guardtime.envelope.annotation;

import com.guardtime.envelope.AbstractEnvelopeTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

}
