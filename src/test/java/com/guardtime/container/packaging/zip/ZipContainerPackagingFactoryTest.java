package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ZipContainerPackagingFactoryTest extends AbstractContainerTest {
    private List<ContainerAnnotation> annotations = new LinkedList<>();
    private List<ContainerDocument> documents = asList(TEST_DOCUMENT_HELLO_TEXT);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        annotations.add(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME));
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
        ZipContainerPackagingFactory containerFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory);
        containerFactory.create(new ArrayList<ContainerDocument>(), annotations);
    }

    @Test
    public void testCreateContainerWithDocument() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, new IncrementingIndexProviderFactory(), true);
        ZipContainer container = packagingFactory.create(documents, null);
        assertNotNull(container);
    }

    @Test
    public void testCreateContainerWithDocumentAndAnnotations() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, new IncrementingIndexProviderFactory(), true);
        ZipContainer container = packagingFactory.create(documents, annotations);
        assertNotNull(container);
    }

    @Test
    public void testCreateContainerWithMultipleDocumentsWithSameName() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        List<ContainerDocument> containerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT),
                new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, mockIndexProviderFactory, true);
        packagingFactory.create(containerDocuments, null);
    }

    @Test
    public void testCreateVerifiesContainer_OK() throws Exception {
        final DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
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
        ZipContainer container = packagingFactory.create(documents, annotations);
        assertNotNull(container);
    }

    @Test
    public void testCreateVerifiesInvalidContainer_NOK() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory());
        packagingFactory.create(documents, annotations);
    }

    @Test
    public void testCreateContainerWithExistingContainerWithDocumentsWithSameName() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        List<ContainerDocument> containerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, new IncrementingIndexProviderFactory(), true);
        Container existingContainer = packagingFactory.create(containerDocuments, null);

        List<ContainerDocument> newContainerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("MoreImportantDocument-0411".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        packagingFactory.create(existingContainer, newContainerDocuments, null);
    }

    @Test
    public void testCreateContainerWithExistingContainerWithSameDocument() throws Exception {
        List<ContainerDocument> containerDocuments = Arrays.asList(
                (ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes()), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, new IncrementingIndexProviderFactory(), true);
        Container container = packagingFactory.create(containerDocuments, null);
        Container newContainer = packagingFactory.create(container, containerDocuments, null);


        Set<String> documentPaths = new HashSet<>();
        for(SignatureContent content : newContainer.getSignatureContents()) {
            documentPaths.addAll(content.getDocuments().keySet());
        }
        assertEquals(1, documentPaths.size());
    }

}