package com.guardtime.container.packaging.zip;

import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ZipContainerReaderTest {

    private ZipContainerReader reader;

    @Mock
    private SignatureFactory signatureFactory;

    private ContainerSignature mockedSignature = new ContainerSignature() {

        @Override
        public void writeTo(OutputStream output) {
            try {
                output.write("TEST-SIGNATURE".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(signatureFactory.create(Mockito.any(DataHash.class))).thenReturn(mockedSignature);
        when(signatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedSignature);
        this.reader = new ZipContainerReader(new TlvContainerManifestFactory(), signatureFactory);
    }

//    @Test
//    public void testReadContainerFile() throws Exception {
//        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource("hello.ksie").toURI()));
//        ZipBlockChainContainer result = reader.read(input);
//        assertNotNull(result);
//        assertNotNull(result.getSignatureContents());
//
//    }

}