package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ZipContainerPackagingFactoryTest extends AbstractContainerTest {
    private static final DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
    private List<ContainerAnnotation> containerAnnotationList = new ArrayList<>();
    private List<ContainerDocument> containerDocumentList = Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        containerAnnotationList.add(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME));
    }

    private ZipContainer createInternallyValidContainer(List<ContainerDocument> documents, List<ContainerAnnotation> annotations) throws Exception {
        return createInternallyValidContainer(documents, annotations, null);
    }

    private ZipContainer createInternallyValidContainer(List<ContainerDocument> documents, List<ContainerAnnotation> annotations, Container existingContainer) throws Exception {
        TlvContainerManifestFactory manifestFactorySpy = spy(new TlvContainerManifestFactory());
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, manifestFactorySpy);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Manifest spyManifest = spy((Manifest) invocationOnMock.callRealMethod());
                doReturn(nullDataHash).when(spyManifest).getDataHash(any(HashAlgorithm.class));
                return spyManifest;
            }
        }).when(manifestFactorySpy).createManifest(any(Pair.class), any(Pair.class), any(Pair.class));
        ContainerSignature mockSignature = mock(ContainerSignature.class);
        when(mockSignature.getSignature()).thenReturn("I decree this to be authentic!");
        when(mockSignature.getSignedDataHash()).thenReturn(nullDataHash);
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mockSignature);
        if (existingContainer != null) {
            return packagingFactory.create(existingContainer, documents, annotations);
        }
        return packagingFactory.create(documents, annotations);
    }

    @Test
    public void testCreatePackagingFactoryWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory must be present");
        new ZipContainerPackagingFactory(null, mockedManifestFactory);
    }

    @Test
    public void testCreatePackagingFactoryWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new ZipContainerPackagingFactory(mockedSignatureFactory, null);
    }


    @Test
    public void testCreatePackagingFactoryWithoutDocuments_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files must not be empty");
        createInternallyValidContainer(new ArrayList<ContainerDocument>(), containerAnnotationList);
    }

    @Test
    public void testCreateContainerWithDocument() throws Exception {
        ZipContainer container = createInternallyValidContainer(containerDocumentList, null);
        assertNotNull(container);
    }

    @Test
    public void testCreateContainerWithDocumentAndAnnotation() throws Exception {
        ZipContainer container = createInternallyValidContainer(containerDocumentList, containerAnnotationList);
        assertNotNull(container);
    }

    @Test
    public void testCreateContainerWithMultipleDocuments() throws Exception {
        List<ContainerDocument> documentsList = new ArrayList<>(containerDocumentList);
        documentsList.add(TEST_DOCUMENT_HELLO_PDF);
        ZipContainer container = createInternallyValidContainer(documentsList, null);
        assertNotNull(container);
        Collection<ContainerDocument> containedDocuments = container.getSignatureContents().get(0).getDocuments().values();
        assertNotNull(containedDocuments);
        assertTrue(containedDocuments.containsAll(documentsList));
    }

    @Test
    public void testCreateContainerWithMultipleDocumentsAndAnnotations() throws Exception {
        List<ContainerDocument> documentsList = new ArrayList<>(containerDocumentList);
        documentsList.add(TEST_DOCUMENT_HELLO_PDF);
        List<ContainerAnnotation> annotationsList = new ArrayList<>(containerAnnotationList);
        annotationsList.add(new StringContainerAnnotation(ContainerAnnotationType.VALUE_REMOVABLE, "moreContent", "com.guardtime.test.inner"));
        ZipContainer container = createInternallyValidContainer(documentsList, annotationsList);
        assertNotNull(container);
        Collection<ContainerAnnotation> containedAnnotations = container.getSignatureContents().get(0).getAnnotations().values();
        assertNotNull(containedAnnotations);
        assertTrue(containedAnnotations.containsAll(annotationsList));
    }

    @Test
    public void testCreateContainerWithExistingContainerAndMultipleDocumentsAndAnnotations() throws Exception {
        Container container = createInternallyValidContainer(containerDocumentList, containerAnnotationList);
        List<ContainerDocument> documentsList = new ArrayList<>();
        documentsList.add(TEST_DOCUMENT_HELLO_PDF);
        documentsList.add(new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_DOC));
        List<ContainerAnnotation> annotationsList = new ArrayList<>(containerAnnotationList);
        annotationsList.add(new StringContainerAnnotation(ContainerAnnotationType.VALUE_REMOVABLE, "moreContent", "com.guardtime.test.inner"));
        ZipContainer newContainer = createInternallyValidContainer(documentsList, annotationsList, container);
        assertNotNull(newContainer);
        Collection<ContainerDocument> containedDocuments = newContainer.getSignatureContents().get(1).getDocuments().values();
        assertNotNull(containedDocuments);
        assertTrue(containedDocuments.containsAll(documentsList));
        Collection<ContainerAnnotation> containedAnnotations = newContainer.getSignatureContents().get(1).getAnnotations().values();
        assertNotNull(containedAnnotations);
        assertTrue(containedAnnotations.containsAll(annotationsList));
    }

    @Test
    public void testCreateContainerWithMultipleDocumentsWithSameName() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Multiple documents with same name found!");

        List<ContainerDocument> containerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT),
                new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        createInternallyValidContainer(containerDocuments, null);
    }

    @Test
    public void testCreateVerifiesContainer_OK() throws Exception {
        assertNotNull(createInternallyValidContainer(containerDocumentList, containerAnnotationList));
    }

    @Test
    public void testCreateVerifiesInvalidContainer_NOK() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Created Container does not pass internal verification");
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory());
        packagingFactory.create(containerDocumentList, containerAnnotationList);
    }

    @Test
    public void testCreateWithExistingContainerVerifiesContainer_OK() throws Exception {
        List<ContainerDocument> containerDocuments = Collections.singletonList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_PDF)
        );
        ZipContainer internallyValidContainer = createInternallyValidContainer(containerDocumentList, containerAnnotationList);
        ZipContainer newContainer = createInternallyValidContainer(containerDocuments, containerAnnotationList, internallyValidContainer);
        assertNotNull(newContainer);
    }

    @Test
    public void testCreateWithExistingContainerVerifiesInvalidContainer_NOK() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Created Container does not pass internal verification");
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory());
        Container mockContainer = Mockito.mock(ZipContainer.class);
        when(mockContainer.getMimeType()).thenReturn(new MimeTypeEntry("MIMETYPE", "Ploomimoos".getBytes()));
        packagingFactory.create(mockContainer, containerDocumentList, containerAnnotationList);
    }

    @Test
    public void testCreateContainerWithExistingContainerWithDocumentsWithSameName() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Multiple documents with same name found!");

        List<ContainerDocument> containerDocuments = Collections.singletonList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        Container existingContainer = createInternallyValidContainer(containerDocuments, null);

        List<ContainerDocument> newContainerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("MoreImportantDocument-0411".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        createInternallyValidContainer(newContainerDocuments, null, existingContainer);
    }

    @Test
    public void testCreateContainerWithExistingContainerWithSameDocument() throws Exception {
        List<ContainerDocument> containerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        Container container = createInternallyValidContainer(containerDocuments, null);
        Container newContainer = createInternallyValidContainer(containerDocuments, null, container);


        Set<String> documentPaths = new HashSet<>();
        for (SignatureContent content : newContainer.getSignatureContents()) {
            documentPaths.addAll(content.getDocuments().keySet());
        }
        assertEquals(1, documentPaths.size());
    }

}