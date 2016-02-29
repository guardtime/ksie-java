package com.guardtime.container;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;


public class BlockChainContainerBuilderTest {

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
    private ZipContainerPackagingFactory packagingFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(signatureFactory.create(Mockito.any(DataHash.class))).thenReturn(mockedSignature);
        ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
        this.packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
    }

    @Test
    public void testCreateSignature() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(packagingFactory);
        builder.withDataFile(new ByteArrayInputStream(new byte[200]), "hello.txt", "text");
        BlockChainContainer container = builder.build();
        container.writeTo(new FileOutputStream("hello.ksie"));
    }

    //TODO move
    @Test
    public void testName() throws Exception {
        BlockChainContainer container = packagingFactory.read(Files.newInputStream(Paths.get(ClassLoader.getSystemResource("test.zip").toURI())));


    }
}