package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractCommonIntegrationTest;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactoryType;
import com.guardtime.ksi.hashing.DataHash;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ZipContainerReaderIT extends AbstractCommonIntegrationTest {
    private ZipContainerReader reader;

    @Before
    public void setUpReader() throws Exception {
        this.reader = new ZipContainerReader(new TlvContainerManifestFactory(), signatureFactory);
    }

    private ZipContainer getContainer(String containerPath) throws IOException, URISyntaxException {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(containerPath).toURI()));
        return reader.read(input);
    }

    @Test
    public void testReadContainerFileWithDocument() throws Exception {
        ZipContainer result = getContainer(CONTAINER_WITH_ONE_DOCUMENT);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            assertFalse(content.getDocuments().isEmpty());
        }
    }

    @Test
    @Ignore
    public void testReadEmptyContainerFile() throws Exception {
        ZipContainer result = getContainer(EMPTY_CONTAINER);
        // TODO: What is the actual expected behaviour for this? Exception or container with no signatureContent and no mimetype?
        assertNotNull(result);
        assertNull(result.getMimeType());
        assertTrue(result.getSignatureContents().isEmpty());
    }

    @Test
    public void testReadContainerFileWithExtraFiles() throws Exception {
        ZipContainer result = getContainer(CONTAINER_WITH_EXTRA_FILES);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        assertFalse(result.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadContainerFileWithoutDocuments() throws Exception {
        ZipContainer result = getContainer(CONTAINER_WITH_NO_DOCUMENTS);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            for (ContainerDocument document : content.getDocuments().values()) {
                assertTrue(document instanceof EmptyContainerDocument);
            }
        }
    }

    @Test
    public void testReadContainerFileWithMultipleAnnotations() throws Exception {
        ZipContainer result = getContainer(CONTAINER_WITH_MULTIPLE_ANNOTATIONS);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        for (SignatureContent content : result.getSignatureContents()) {
            assertTrue(content.getAnnotations().size() > 1);
        }
    }

    @Test
    public void testReadContainerFileWithMultipleSignatures() throws Exception {
        ZipContainer result = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        assertNotNull(result);
        assertTrue(result.getSignatureContents().size() > 1);
    }

    @Test
    public void testReadContainerFileWithBrokenSignatures() throws Exception {
        ZipContainer result = getContainer(CONTAINER_WITH_BROKEN_SIGNATURE);
        assertNotNull(result);
        assertFalse(result.getSignatureContents().isEmpty());
        assertFalse(result.getUnknownFiles().isEmpty());
    }

}