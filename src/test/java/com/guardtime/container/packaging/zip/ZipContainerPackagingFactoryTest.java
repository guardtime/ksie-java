package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ZipContainerPackagingFactoryTest extends AbstractContainerTest {

    @Mock
    protected ContainerDocument mockedDataFile;

    @Mock
    private ContainerAnnotation mockedAnnotation;

    private List<ContainerAnnotation> annotations = new LinkedList<>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedAnnotation.getAnnotationType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE);
        when(mockedAnnotation.getDomain()).thenReturn(ANNOTATION_DOMAIN_COM_GUARDTIME);
        annotations.add(new StringAnnotation(ContainerAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME));
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
    public void testCreatePackagingFactoryWithoutDataFiles_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Data files must not be empty");
        ZipContainerPackagingFactory containerFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory);
        containerFactory.create(new ArrayList<ContainerDocument>(), annotations);
    }

//TODO
//    @Test
//    public void testCreateContainerWithDataFile() throws Exception {
//        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory);
//        ZipBlockChainContainer container = packagingFactory.create(asList(TEST_DOCUMENT_HELLO_TEXT), null);
//        assertNotNull(container);
//
//    }

    //TODO add tests: create container without annotations
    //TODO add tests: create container with multiple annotations

}