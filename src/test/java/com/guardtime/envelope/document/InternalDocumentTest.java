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
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("EnvelopeElement must be present");
        new InternalDocument(null);
    }

    @Test
    public void testNullFileName() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File name must be present");
        AbstractDocument mockDoc = Mockito.mock(AbstractDocument.class);
        new InternalDocument(mockDoc);
    }

    @Test
    public void testNullElement() throws IOException {
        AbstractDocument mockDoc = Mockito.mock(AbstractDocument.class);
        Mockito.when(mockDoc.getPath()).thenReturn("Doc");
        InternalDocument document = new InternalDocument(mockDoc);
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("InternalDocument has null InputStream");
        document.getInputStream();
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
