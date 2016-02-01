package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.BlockChainContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class AbstractBlockChainContainerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected SignatureFactory mockedSignatureFactory;

    @Mock
    protected ContainerManifestFactory mockedManifestFactory;

    @Mock
    protected BlockChainContainerPackagingFactory mockedPackagingFactory;

    @Mock
    protected AnnotationInfoManifest annotationInfoManifest;

    @Mock
    protected AnnotationsManifest mockedAnnotationsManifest;

    @Mock
    protected DataFilesManifest mockedDataFileManifest;

    @Mock
    protected SignatureManifest mockedSignatureManifest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockedManifestFactory.createAnnotationsManifest(Mockito.anyMapOf(ContainerAnnotation.class, AnnotationInfoManifest.class), Mockito.anyString())).thenReturn(mockedAnnotationsManifest);
        when(mockedManifestFactory.createAnnotationManifest(Mockito.any(DataFilesManifest.class), Mockito.any(ContainerAnnotation.class), Mockito.anyString())).thenReturn(annotationInfoManifest);
        when(mockedManifestFactory.createDataFilesManifest(Mockito.anyListOf(ContainerDocument[].class), Mockito.anyString())).thenReturn(mockedDataFileManifest);
        when(mockedManifestFactory.createSignatureManifest(Mockito.any(DataFilesManifest.class), Mockito.any(AnnotationsManifest.class), Mockito.anyString())).thenReturn(mockedSignatureManifest);
    }

}
