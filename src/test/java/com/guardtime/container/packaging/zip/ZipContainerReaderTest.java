package com.guardtime.container.packaging.zip;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactoryType;
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

import static org.junit.Assert.*;
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
        when(signatureFactory.getSignatureFactoryType()).thenReturn(new KsiSignatureFactoryType());
        when(signatureFactory.create(Mockito.any(DataHash.class))).thenReturn(mockedSignature);
        when(signatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedSignature);
        this.reader = new ZipContainerReader(new TlvContainerManifestFactory(), signatureFactory);
    }

    @Test
    public void testReadContainerFileWithDocument() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource("containers/container-one-file.ksie").toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            assertFalse(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadEmptyContainerFile() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource("containers/container-empty.ksie").toURI()));
        ZipBlockChainContainer result = reader.read(input);
        // TODO: What is the actual expected behaviour for this? Exception or container with no signatureContent and no mimetype?
        assertNotNull(result);
        assertTrue(result.getSignatureContents().isEmpty());
    }

    @Test
    public void testReadContainerFileWithExtraFiles() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource("containers/container-extra-files.ksie").toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        assertFalse(result.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadContainerFileWithoutDocuments() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource("containers/container-no-documents.ksie").toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            for (ContainerDocument document : content.getDocuments()) {
                assertTrue(document instanceof EmptyContainerDocument);
            }
        }
    }

    @Test
    public void testReadContainerFileWithMultipleAnnotations() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource("containers/container-one-file-3-annotations.ksie").toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            assertTrue(content.getAnnotations().size() > 1);
        }
    }

    // test empty container                 --- DONE!
    // test extra files container           --- DONE!
    // test no documents container          --- DONE!
    // test one document container          --- DONE!
    // test multiple annotations container  --- DONE!
    // test multiple signatures container // TODO: get container with multiple signatures
    // test broken container (missing files)  // TODO: make multiple signature container and break one of them, preferably at random  (look into executing shell commands ExecuteShellCommand com = new ExecuteShellCommand();System.out.println(com.executeCommand("ls"));)

}