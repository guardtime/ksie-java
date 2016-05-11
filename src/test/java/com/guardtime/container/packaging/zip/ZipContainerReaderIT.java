package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractCommonIntegrationTest;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.SignatureContent;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class ZipContainerReaderIT extends AbstractCommonIntegrationTest {
    private ZipContainerReader reader;

    @Before
    public void setUpReader() throws Exception {
        this.reader = new ZipContainerReader(manifestFactory, signatureFactory);
    }

    private ZipContainer getContainer(String containerPath) throws Exception {
        InputStream input = new FileInputStream(loadFile(containerPath));
        return reader.read(input);
    }

    @Test
    public void testReadContainerFileWithDocument() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_ONE_DOCUMENT);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertFalse(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadEmptyContainerFile_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Container has no valid content");
        ZipContainer container = getContainer(EMPTY_CONTAINER);
    }

    @Test
    public void testReadContainerFileWithExtraFiles() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_EXTRA_FILES);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        assertFalse(container.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadContainerFileWithoutDocuments() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_NO_DOCUMENTS);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            for (ContainerDocument document : content.getDocuments().values()) {
                assertTrue(document instanceof EmptyContainerDocument);
            }
        }
    }

    @Test
    public void testReadContainerFileWithMultipleAnnotations() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_MULTIPLE_ANNOTATIONS);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertTrue(content.getAnnotations().size() > 1);
        }
    }

    @Test
    public void testReadContainerFileWithMultipleSignatures() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        assertNotNull(container);
        assertTrue(container.getSignatureContents().size() > 1);
    }

    @Test
    public void testReadContainerFileWithBrokenSignatures() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_BROKEN_SIGNATURE);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        assertFalse(container.getUnknownFiles().isEmpty());
    }

}