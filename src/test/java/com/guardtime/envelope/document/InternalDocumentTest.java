package com.guardtime.envelope.document;

import com.guardtime.envelope.AbstractEnvelopeTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.guardtime.ksi.util.Util.toByteArray;
import static junit.framework.TestCase.assertTrue;

public class InternalDocumentTest extends AbstractEnvelopeTest {

    @Test
    public void testElementNull() {
        //TODO: Add expected results.
        new InternalDocument(null);
    }

    @Test
    public void testNullFileName() {
        //TODO: Add expected results.
        AbstractDocument mockDoc = Mockito.mock(AbstractDocument.class);
        new InternalDocument(mockDoc);
    }

    @Test
    public void testCompareStreams() throws IOException {
        StreamDocument document = new StreamDocument(new ByteArrayInputStream(new byte[32]), "doc", "doc");
        InternalDocument internalDocument = new InternalDocument(document);

        assertTrue(
                Arrays.equals(
                        toByteArray(document.getInputStream()),
                        toByteArray(internalDocument.getInputStream())
                )
        );
    }
}
