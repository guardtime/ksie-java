package com.guardtime.envelope.document;

import com.guardtime.envelope.AbstractEnvelopeTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;


public class DocumentFactoryTest extends AbstractEnvelopeTest  {

    @Test
    public void testCreateWithoutParsingStore_ThrownNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store must be present");
        new DocumentFactory(null);
    }

    @Test
    public void testCopy_OK() {
        DocumentFactory factory = new DocumentFactory(parsingStore);
        Document newDocument = factory.create(testDocumentHelloText);
        assertNotSame(testDocumentHelloText, newDocument);
        assertEquals(testDocumentHelloText, newDocument);
    }

}
