package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZipEnvelopeWriterTest extends AbstractEnvelopeTest {

    @Test
    public void testAddDocumentWithDirectoryName_ThrowsIOException() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Invalid document filename!");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mock(EnvelopeSignature.class));
        EnvelopeDocument testDocument = new StreamEnvelopeDocument(new ByteArrayInputStream(new byte[0]), "some type", "folder/");
        try (Envelope envelope = packagingFactory.create(singletonList(testDocument), singletonList(STRING_ENVELOPE_ANNOTATION))) {
            ZipEnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
        }
    }

}