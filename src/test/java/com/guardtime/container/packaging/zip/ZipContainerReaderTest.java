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

    private static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-file.ksie";
    private static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    private static final String CONTAINER_WITH_EXTRA_FILES = "containers/container-extra-files.ksie";
    private static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    private static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    private static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    private static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";
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
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_ONE_DOCUMENT).toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            assertFalse(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadEmptyContainerFile() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(EMPTY_CONTAINER).toURI()));
        ZipBlockChainContainer result = reader.read(input);
        // TODO: What is the actual expected behaviour for this? Exception or container with no signatureContent and no mimetype?
        assertNotNull(result);
        assertTrue(result.getSignatureContents().isEmpty());
    }

    @Test
    public void testReadContainerFileWithExtraFiles() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_EXTRA_FILES).toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        assertFalse(result.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadContainerFileWithoutDocuments() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_NO_DOCUMENTS).toURI()));
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
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_MULTIPLE_ANNOTATIONS).toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            assertTrue(content.getAnnotations().size() > 1);
        }
    }

    @Test
    public void testReadContainerFileWithMultipleSignatures() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_MULTIPLE_SIGNATURES).toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertTrue(result.getSignatureContents().size() > 1);
    }

    @Test
    public void testReadContainerFileWithBrokenSignatures() throws Exception {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_BROKEN_SIGNATURE).toURI()));
        ZipBlockChainContainer result = reader.read(input);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        assertFalse(result.getUnknownFiles().isEmpty());
    }

}