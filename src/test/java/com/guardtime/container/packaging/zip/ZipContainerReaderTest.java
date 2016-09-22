package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ZipContainerReaderTest extends AbstractContainerTest {
    private ZipContainerReader reader;

    protected static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    protected static final String CONTAINER_WITH_EXTRA_FILES = "containers/container-extra-files.ksie";
    protected static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    protected static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-file.ksie";
    protected static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION = "containers/container-missing-annotation.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION_DATA = "containers/container-missing-annotation-data.ksie";
    protected static final String CONTAINERS_CONTAINER_INVALID_ANNOTATION_TYPE = "containers/container-invalid-annotation-type.ksie";
    protected static final String CONTAINERS_CONTAINER_DOCUMENT_MISSING_MIMETYPE = "containers/container-document-missing-mimetype.ksie";
    protected static final String CONTAINERS_CONTAINER_NO_DOCUMENT_URI_IN_MANIFEST = "containers/container-no-document-uri-in-manifest.ksie";

    @Mock
    protected KSI mockKsi;

    @Before
    public void setUpReader() throws Exception {
        ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();

        when(mockKsi.sign(Mockito.any(DataHash.class))).thenReturn(Mockito.mock(KSISignature.class));
        when(mockKsi.extend(Mockito.any(KSISignature.class))).thenReturn(Mockito.mock(KSISignature.class));
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
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
        expectedException.expectMessage("Parsed container was not valid");
        getContainer(EMPTY_CONTAINER);
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
    public void testReadContainerFileWithMissingDocumentUri() throws Exception {
        ZipContainer container = getContainer(CONTAINERS_CONTAINER_NO_DOCUMENT_URI_IN_MANIFEST);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertTrue(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadContainerFileWithMissingDocumentMIMEType() throws Exception {
        ZipContainer container = getContainer(CONTAINERS_CONTAINER_DOCUMENT_MISSING_MIMETYPE);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertTrue(content.getDocuments().isEmpty());
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
    public void testReadContainerFileWithInvalidAnnotationType() throws Exception {
        ZipContainer container = getContainer(CONTAINERS_CONTAINER_INVALID_ANNOTATION_TYPE);
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

    @Test
    public void testReadContainerFileWithMissingAnnotationData() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_MISSING_ANNOTATION_DATA);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
    }

    @Test
    public void testReadContainerFileWithMissingAnnotation() throws Exception {
        ZipContainer container = getContainer(CONTAINER_WITH_MISSING_ANNOTATION);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
    }
}