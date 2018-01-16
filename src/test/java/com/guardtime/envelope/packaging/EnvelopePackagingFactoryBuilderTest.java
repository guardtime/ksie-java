package com.guardtime.envelope.packaging;

import com.guardtime.envelope.AbstractEnvelopeTest;

import org.junit.Test;

public class EnvelopePackagingFactoryBuilderTest extends AbstractEnvelopeTest {

    @Test
    public void testCreatePackagingFactoryWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory must be present");
        new EnvelopePackagingFactory
                .Builder()
                .withSignatureFactory(null)
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new EnvelopePackagingFactory
                .Builder()
                .withSignatureFactory(mockedSignatureFactory)
                .withManifestFactory(null)
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutParsingStoreFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store factory must be present");
        new EnvelopePackagingFactory
                .Builder()
                .withSignatureFactory(mockedSignatureFactory)
                .withManifestFactory(mockedManifestFactory)
                .withParsingStoreFactory(null)
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutEnvelopeReader_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Envelope reader must be present");
        new EnvelopePackagingFactory
                .Builder()
                .withSignatureFactory(mockedSignatureFactory)
                .withEnvelopeReader(null)
                .build();
    }

}
