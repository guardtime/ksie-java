package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractBlockChainContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ZipContainerPackagingFactoryTest extends AbstractBlockChainContainerTest {

    protected static final String ANNOTATION_INFO_MANIFEST_NAME = "META-INF/annotmanifest1.tlv";
    protected static final String DATA_FILES_MANIFEST_NAME = "META-INF/datamanifest.tlv";
    protected static final String SIGNATURE_MANIFEST1_NAME = "META-INF/manifest1.tlv";
    @Mock
    protected ContainerDocument mockedDataFile;
    @Mock
    private ContainerAnnotation mockedAnnotation;
    private List<ContainerAnnotation> annotations = new LinkedList<>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedAnnotation.getAnnotationType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE);
        when(mockedAnnotation.getDomain()).thenReturn("com.guardtime");
        when(mockedAnnotation.getMimeType()).thenReturn("application/txt");
        annotations.add(new StringAnnotation(ContainerAnnotationType.NON_REMOVABLE, "AnnotContent1", "com.guardtime.annot"));
    }

    @Test
    public void testReadSignature() throws Exception {
        ZipContainerPackagingFactory factory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory());
        factory.read(Files.newInputStream(Paths.get(ClassLoader.getSystemResource("test.zip").toURI())));
    }

    @Test
    public void testCreateContainerWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory must be present");
        new ZipContainerPackagingFactory(null, mockedManifestFactory);
    }

    @Test
    public void testCreateContainerWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new ZipContainerPackagingFactory(mockedSignatureFactory, null);
    }


    @Test
    public void testCreateContainerWithoutDataFiles_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Data files must not be empty");
        ZipContainerPackagingFactory containerFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory);
        containerFactory.create(new ArrayList<ContainerDocument>(), annotations);
    }

//    @Test
//    public void testCreateNewContainer_Ok() throws Exception {
//        when(mockedDataFile.getFileName()).thenReturn("data1.txt");
//        when(mockedDataFile.getInputStream()).thenReturn(new ByteArrayInputStream("42".getBytes()));
//
//
//        when(annotationInfoManifest.getUri()).thenReturn("annot1manifest");
//        when(annotationInfoManifest.getInputStream()).thenReturn(new ByteArrayInputStream("amanifest".getBytes()));
//
//        when(mockedAnnotationsManifest.getUri()).thenReturn(ANNOTATION_INFO_MANIFEST_NAME);
//        when(mockedAnnotationsManifest.getInputStream()).thenReturn(new ByteArrayInputStream("mocked-annotation-manifest".getBytes()));
//        when(mockedDataFileManifest.getUri()).thenReturn(DATA_FILES_MANIFEST_NAME);
//        when(mockedDataFileManifest.getInputStream()).thenReturn(new ByteArrayInputStream("mocked-datafile-manifest".getBytes()));
//        when(mockedSignatureManifest.getUri()).thenReturn(SIGNATURE_MANIFEST1_NAME);
//        when(mockedSignatureManifest.getInputStream()).thenReturn(new ByteArrayInputStream("mocked-signature-manifest".getBytes()));
//
//        ZipContainerPackagingFactory containerFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory);
//        ZipBlockChainContainer container = containerFactory.create(Arrays.asList(new ContainerDocument[]{mockedDataFile}), annotations);
//
//        assertNotNull(container);
//        verify(mockedManifestFactory, ONE_EXECUTION).createAnnotationManifest(Mockito.any(DataFilesManifest.class), Mockito.any(ContainerAnnotation.class), Mockito.anyString());
//        verify(mockedManifestFactory, ONE_EXECUTION).createAnnotationsManifest(Mockito.anyMapOf(ContainerAnnotation.class, AnnotationInfoManifest.class), Mockito.anyString());
//        verify(mockedManifestFactory, ONE_EXECUTION).createDataFilesManifest(Mockito.anyListOf(ContainerDocument[].class), Mockito.anyString());
//        verify(mockedManifestFactory, ONE_EXECUTION).createSignatureManifest(Mockito.any(DataFilesManifest.class), Mockito.any(AnnotationsManifest.class), Mockito.anyString(), Mockito.anyString());
//    }

    //TODO add tests: create container without data file(s)
    //TODO add tests: create container without annotations
    //TODO add tests: create container with multiple annotations

}