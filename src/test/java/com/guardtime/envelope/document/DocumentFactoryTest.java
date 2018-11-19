package com.guardtime.envelope.document;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class DocumentFactoryTest extends AbstractEnvelopeTest  {

    @Test
    public void testCreateWithoutParsingStore_ThrownNullPointerException() {
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

    @Test
    public void testCreateWithFile_OK() throws Exception {
        String fileName = "newFilename.doc";
        DocumentFactory factory = new DocumentFactory(parsingStore);
        Document document = factory.create(loadFile(""), "Doc", fileName);
        assertNotNull(document);
        assertEquals(fileName, document.getFileName());
    }

    @Test
    public void testCreateDocumentWhereOriginalIsInstanceOfFileDocument_OK() throws Exception {
        String fileName = "newFilename.doc";
        DocumentFactory factory = new DocumentFactory(parsingStore);
        Document document = factory.create(loadFile(TEST_FILE_PATH_TEST_TXT), "Doc", fileName);
        Document newDocument = factory.create(document);
        assertNotSame(document, newDocument);
        assertEquals(document, newDocument);
    }

    @Test
    public void testCreateDocumentWhereOriginalIsInstanceOfInternalDocument_OK() throws DataHashException, IOException {
        DocumentFactory factory = new DocumentFactory(parsingStore);

        EnvelopeElement element = Mockito.mock(EnvelopeElement.class);
        when(element.getPath())
                .thenReturn("Element.path");
        when(element.getDataHash(any(HashAlgorithm.class)))
                .thenReturn(new DataHash(HashAlgorithm.SHA2_256, new byte[HashAlgorithm.SHA2_256.getLength()]));
        when(element.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        Document document = factory.create(element);

        Document newDocument = factory.create(document);
        assertNotSame(document, newDocument);
        assertEquals(document, newDocument);

        assertEquals(element.getDataHash(HashAlgorithm.SHA2_256),
                newDocument.getDataHashList(Collections.singletonList(HashAlgorithm.SHA2_256)).get(0));
        assertEquals(element.getPath(), newDocument.getFileName());
    }
}
