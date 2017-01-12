package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.parsing.TemporaryFileBasedParsingStoreFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ZipContainerReaderTest extends AbstractContainerTest {
    @Mock
    protected KSI mockKsi;

    private ZipContainerReader reader;
    private Container container;


    @Before
    public void setUpReader() throws Exception {
        ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();

        when(mockKsi.sign(Mockito.any(DataHash.class))).thenReturn(Mockito.mock(KSISignature.class));
        when(mockKsi.extend(Mockito.any(KSISignature.class))).thenReturn(Mockito.mock(KSISignature.class));
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        this.reader = new ZipContainerReader(manifestFactory, signatureFactory, new TemporaryFileBasedParsingStoreFactory().create());
    }

    @After
    public void closeContainer() throws Exception {
        if (container != null) {
            container.close();
        }
    }

    private void setUpContainer(String containerPath) throws Exception {
        try (InputStream input = new FileInputStream(loadFile(containerPath))) {
            this.container = reader.read(input);
        }
    }

    @Test
    public void testReadContainerFileWithDocument() throws Exception {
        setUpContainer(CONTAINER_WITH_ONE_DOCUMENT);
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
        setUpContainer(EMPTY_CONTAINER);
    }

    @Test
    public void testReadContainerFileWithExtraFiles() throws Exception {
        setUpContainer(CONTAINER_WITH_UNKNOWN_FILES);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        assertFalse(container.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadContainerFileWithoutDocuments() throws Exception {
        setUpContainer(CONTAINER_WITH_NO_DOCUMENTS);
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
        setUpContainer(CONTAINER_WITH_NO_DOCUMENT_URI_IN_MANIFEST);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertTrue(content.getDocuments().isEmpty());
            assertFalse(content.getDocumentsManifest().getRight().getDocumentReferences().isEmpty());
        }
    }

    @Test
    public void testReadContainerFileWithMissingDocumentMIMEType() throws Exception {
        setUpContainer(CONTAINER_WITH_DOCUMENT_MISSING_MIMETYPE);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertTrue(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadContainerFileWithMultipleAnnotations() throws Exception {
        setUpContainer(CONTAINER_WITH_MULTIPLE_ANNOTATIONS);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            assertTrue(content.getAnnotations().size() > 1);
        }
    }

    @Test
    public void testReadContainerFileWithInvalidAnnotationType() throws Exception {
        setUpContainer(CONTAINER_WITH_INVALID_ANNOTATION_TYPE);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        for (SignatureContent content : container.getSignatureContents()) {
            AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
            int insertedAnnotationsCount = annotationsManifest.getSingleAnnotationManifestReferences().size();
            int parsableAnnotationsCount = content.getAnnotations().size();
            assertTrue(parsableAnnotationsCount < insertedAnnotationsCount);
        }
    }

    @Test
    public void testReadContainerFileWithMultipleSignatures() throws Exception {
        setUpContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        assertNotNull(container);
        assertTrue(container.getSignatureContents().size() > 1);
    }

    @Test
    public void testReadContainerFileWithBrokenSignatures() throws Exception {
        setUpContainer(CONTAINER_WITH_BROKEN_SIGNATURE_CONTENT);
        assertNotNull(container);
        assertFalse(container.getSignatureContents().isEmpty());
        assertFalse(container.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadContainerFileWithMissingAnnotationData() throws Exception {
        setUpContainer(CONTAINER_WITH_MISSING_ANNOTATION_DATA);
        assertNotNull(container);
        SignatureContent signatureContent = container.getSignatureContents().get(0);
        AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest().getRight();
        assertFalse(container.getSignatureContents().isEmpty());
        assertTrue(signatureContent.getAnnotations().isEmpty());
        assertFalse(signatureContent.getSingleAnnotationManifests().isEmpty());
        assertFalse(annotationsManifest.getSingleAnnotationManifestReferences().isEmpty());
    }

    @Test
    public void testReadContainerFileWithMissingAnnotation() throws Exception {
        setUpContainer(CONTAINER_WITH_MISSING_ANNOTATION);
        assertNotNull(container);
        SignatureContent signatureContent = container.getSignatureContents().get(0);
        AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest().getRight();
        assertFalse(container.getSignatureContents().isEmpty());
        assertTrue(signatureContent.getAnnotations().isEmpty());
        assertTrue(signatureContent.getSingleAnnotationManifests().isEmpty());
        assertFalse(annotationsManifest.getSingleAnnotationManifestReferences().isEmpty());
    }
}