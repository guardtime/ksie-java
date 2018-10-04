package com.guardtime.envelope.packaging.parsing.store;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.ksi.util.Util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParsingStoreReferenceTest extends AbstractEnvelopeTest {

    @Test
    public void testCreateWithoutId_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("UUID must be present");
        new ParsingStoreReference(null, parsingStore, "name");
    }

    @Test
    public void testCreateWithoutParsingStore_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store must be present");
        new ParsingStoreReference(UUID.randomUUID(), null, "key");
    }

    @Test
    public void testCreateWithoutName_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Path name must be present");
        new ParsingStoreReference(UUID.randomUUID(), parsingStore, null);
    }

    @Test
    public void testAccessingStoredContent_OK() throws Exception {
        ParsingStoreReference reference = null;
        String originalContent = "someContent";
        try (ByteArrayInputStream bis = new ByteArrayInputStream(originalContent.getBytes())) {
            reference = parsingStore.store(bis, "somePath");
        }

        try (InputStream stream = reference.getStoredContent();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Util.copyData(stream, bos);
            String retrievedContent = new String(bos.toByteArray());
            assertEquals(originalContent, retrievedContent);
        }
    }

    @Test
    public void testClearParsingStore_OK() throws Exception {
        ParsingStoreReference reference = null;
        String originalContent = "someContent";
        try (ByteArrayInputStream bis = new ByteArrayInputStream(originalContent.getBytes())) {
            reference = parsingStore.store(bis, "somePath");
        }

        try (InputStream stream = parsingStore.getContent(reference.getUuid());
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Util.copyData(stream, bos);
            String retrievedContent = new String(bos.toByteArray());
            assertEquals(originalContent, retrievedContent);
        }

        reference.unstore();
        try (InputStream stream = parsingStore.getContent(reference.getUuid())) {

        } catch (IllegalStateException e) {
            // Expected behaviour since the content is not there anymore!
            assertTrue(e.getMessage().contains("Parsing store has lost content for ID '" + reference.getUuid().toString() + "'"));
        }
    }
}
