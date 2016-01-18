package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.datafile.ContainerDataFile;
import com.guardtime.container.manifest.*;
import com.guardtime.container.signature.SignatureFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ZipContainerPackagingFactoryTest {

    protected static final VerificationMode ONE_EXECUTION = times(1);
    protected static final String ANNOTATION_INFO_MANIFEST_NAME = "META-INF/annotmanifest1.tlv";
    protected static final String DATA_FILES_MANIFEST_NAME = "META-INF/datamanifest.tlv";
    protected static final String SIGNATURE_MANIFEST1_NAME = "META-INF/manifest1.tlv";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private SignatureFactory mockedSignatureFactory;

    @Mock
    private ContainerManifestFactory mockedManifestFactory;

    @Mock
    private AnnotationInfoManifest annotationInfoManifest;

    @Mock
    private AnnotationsManifest mockedAnnotationsManifest;

    @Mock
    private DataFilesManifest mockedDataFileManifest;

    @Mock
    private SignatureManifest mockedSignatureManifest;

    @Mock
    private ContainerDataFile mockedDataFile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockedManifestFactory.createAnnotationsManifest(Mockito.anyListOf(AnnotationInfoManifest.class))).thenReturn(mockedAnnotationsManifest);
        when(mockedManifestFactory.createAnnotationManifest(Mockito.any(DataFilesManifest.class), Mockito.any(ContainerAnnotation.class))).thenReturn(annotationInfoManifest);
        when(mockedManifestFactory.createDataFilesManifest(Mockito.anyListOf(ContainerDataFile[].class))).thenReturn(mockedDataFileManifest);
        when(mockedManifestFactory.createSignatureManifest(Mockito.any(DataFilesManifest.class), Mockito.any(AnnotationsManifest.class))).thenReturn(mockedSignatureManifest);
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
        containerFactory.create(new ArrayList<ContainerDataFile>(), Arrays.asList(new ContainerAnnotation[]{new StringContainerAnnotation("name", "com.guardtime.blockchain.container", "42")}));
    }

    @Test
    public void testCreateNewContainer_Ok() throws Exception {
        when(mockedAnnotationsManifest.getUri()).thenReturn(ANNOTATION_INFO_MANIFEST_NAME);
        when(mockedAnnotationsManifest.getInputStream()).thenReturn(new ByteArrayInputStream("mocked-annotation-manifest".getBytes()));
        when(mockedDataFileManifest.getUri()).thenReturn(DATA_FILES_MANIFEST_NAME);
        when(mockedDataFileManifest.getInputStream()).thenReturn(new ByteArrayInputStream("mocked-datafile-manifest".getBytes()));
        when(mockedSignatureManifest.getUri()).thenReturn(SIGNATURE_MANIFEST1_NAME);
        when(mockedSignatureManifest.getInputStream()).thenReturn(new ByteArrayInputStream("mocked-signature-manifest".getBytes()));

        ZipContainerPackagingFactory containerFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory);
        ZipBlockchainContainer container = containerFactory.create(Arrays.asList(new ContainerDataFile[] {mockedDataFile}), Arrays.asList(new ContainerAnnotation[]{new StringContainerAnnotation("name", "com.guardtime.blockchain.container", "42")}));
        assertNotNull(container);
        verify(mockedManifestFactory, ONE_EXECUTION).createAnnotationManifest(Mockito.any(DataFilesManifest.class), Mockito.any(ContainerAnnotation.class));
        verify(mockedManifestFactory, ONE_EXECUTION).createAnnotationsManifest(Mockito.anyListOf(AnnotationInfoManifest.class));
        verify(mockedManifestFactory, ONE_EXECUTION).createDataFilesManifest(Mockito.anyListOf(ContainerDataFile[].class));
        verify(mockedManifestFactory, ONE_EXECUTION).createSignatureManifest(Mockito.any(DataFilesManifest.class), Mockito.any(AnnotationsManifest.class));
    }

    //TODO add tests: create container without data file(s)
    //TODO add tests: create container without annotations
    //TODO add tests: create container with multiple annotations


}