package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.StreamDocument;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZipEnvelopeWriterTest extends AbstractEnvelopeTest {

    @Test
    public void testAddDocumentWithDirectoryName_ThrowsIOException() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage(" is an invalid document file name!");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mock(EnvelopeSignature.class));
        Document testDocument = new StreamDocument(new ByteArrayInputStream(new byte[0]), "some type", "folder/");
        try (Envelope envelope = packagingFactory.create(singletonList(testDocument), singletonList(STRING_ENVELOPE_ANNOTATION))) {
            ZipEnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
        }
    }

}