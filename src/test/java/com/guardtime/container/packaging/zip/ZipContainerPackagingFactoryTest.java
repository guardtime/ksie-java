package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

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
        new ZipContainerPackagingFactory(null, mockedManifestFactory, mockIndexProvider);
    }

    @Test
    public void testCreatePackagingFactoryWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new ZipContainerPackagingFactory(mockedSignatureFactory, null, mockIndexProvider);
    }


    @Test
    public void testCreatePackagingFactoryWithoutDocuments_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files must not be empty");
        ZipContainerPackagingFactory containerFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, mockIndexProvider);
        containerFactory.create(new ArrayList<ContainerDocument>(), annotations);
    }

    @Test
    public void testCreateContainerWithDocument() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, mockIndexProvider);
        ZipContainer container = packagingFactory.create(documents, null);
        assertNotNull(container);
    }

    @Test
    public void testCreateContainerWithDocumentAndAnnotations() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, mockIndexProvider);
        ZipContainer container = packagingFactory.create(documents, annotations);
        assertNotNull(container);
    }

}