package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParsingStoreSessionTest extends AbstractEnvelopeTest {

    @Test
    public void testNullParsingStore_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store must be present");
        new ParsingStoreSession(null);
    }

    @Test
    public void testStoreData_OK() throws Exception {
        ParsingStoreSession session = new ParsingStoreSession(parsingStore);
        try (ByteArrayInputStream bis = new ByteArrayInputStream("testingString".getBytes())) {
            session.store("randomKey", bis);
        }
    }

    @Test
    public void testStoreDataWithExistingKey_ThrowsParsingStoreException() throws Exception {
        expectedException.expect(ParsingStoreException.class);
        expectedException.expectMessage("already used");
        ParsingStoreSession session = new ParsingStoreSession(parsingStore);
        try (ByteArrayInputStream bis = new ByteArrayInputStream("testingString".getBytes());
             ByteArrayInputStream bis2 = new ByteArrayInputStream("testingStringTestingStuff".getBytes())) {
            session.store("randomKey", bis);
            session.store("randomKey", bis2);
        }
    }

    @Test
    public void testStoredKeyCanBeChecked_OK() throws Exception {
        ParsingStoreSession session = new ParsingStoreSession(parsingStore);
        String key = "randomKey";
        assertFalse(session.contains(key));
        try (ByteArrayInputStream bis = new ByteArrayInputStream("testingString".getBytes())) {
            session.store(key, bis);
        }
        assertTrue(session.contains(key));
    }
}
