package com.guardtime.envelope.document;

import com.guardtime.envelope.AbstractEnvelopeTest;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static com.guardtime.ksi.util.Util.toByteArray;
import static junit.framework.TestCase.assertTrue;

public class InternalDocumentTest extends AbstractEnvelopeTest {

    @Test
    public void testElementNull() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("EnvelopeElement must be present");
        new InternalDocument(null);
    }

    @Test
    public void testCompareStreams() throws IOException {
        StreamDocument document = new StreamDocument(new ByteArrayInputStream(new byte[32]), "doc", "doc");
        InternalDocument internalDocument = new InternalDocument(document);

        assertTrue(isEqual(document.getInputStream(), internalDocument.getInputStream()));
    }

    private boolean isEqual(InputStream i1, InputStream i2) throws IOException {
        return Arrays.equals(toByteArray(i1), toByteArray(i2));
    }

}
