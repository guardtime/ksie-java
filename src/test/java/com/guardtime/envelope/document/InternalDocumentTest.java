package com.guardtime.envelope.document;

import com.guardtime.envelope.AbstractEnvelopeTest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

public class InternalDocumentTest extends AbstractEnvelopeTest {

    @Test
    public void testElementNull() {
        new InternalDocument(null);
    }

    @Test
    public void testCompareStreams() throws IOException {
        StreamDocument document = new StreamDocument(new ByteArrayInputStream(new byte[32]), "doc", "doc");
        InternalDocument internalDocument = new InternalDocument(document);

        assertTrue(isEqual(document.getInputStream(), internalDocument.getInputStream()));
    }

    private boolean isEqual(InputStream i1, InputStream i2) throws IOException {
        return Arrays.equals(getBytes(i1), getBytes(i2));
    }

    private byte[] getBytes(InputStream stream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int read;
            byte[] data = new byte[64];

            while ((read = stream.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, read);
            }

            return baos.toByteArray();
        }
    }
}
