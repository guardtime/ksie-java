package com.guardtime.envelope.annotation;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractAnnotationTest {

    /**
     * Class for testing out AbstractAnnotation
     */
    static class TestAnnotation extends AbstractAnnotation {
        private final String content;

        protected TestAnnotation(String domain, EnvelopeAnnotationType type, String content) {
            super(domain, type);
            this.content = content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content.getBytes());
        }
    }

    @Test
    public void testEqualsWithDifferentDomain() {
        Annotation a1 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        Annotation a2 = new TestAnnotation("another.random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEqualsWithDifferentType() {
        Annotation a1 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        Annotation a2 = new TestAnnotation("random.domain", EnvelopeAnnotationType.FULLY_REMOVABLE, "someContent");
        assertFalse(a1.equals(a2));
    }
    @Test
    public void testEqualsWithDifferentPath() {
        Annotation a1 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        Annotation a2 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        a1.setPath("notAnotherAnnotation.dat");
        a2.setPath("someAnnotation.dat");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEqualsWithPathUnset() {
        Annotation a1 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        Annotation a2 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        a2.setPath("someAnnotation.dat");
        assertFalse(a1.equals(a2));
    }
    @Test
    public void testEqualsWithSimilarAnnotations() {
        Annotation a1 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        Annotation a2 = new TestAnnotation("random.domain", EnvelopeAnnotationType.NON_REMOVABLE, "someContent");
        assertTrue(a1.equals(a2));
    }

}
