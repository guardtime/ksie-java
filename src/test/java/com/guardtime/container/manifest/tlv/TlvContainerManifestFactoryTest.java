package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.util.Pair;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TlvContainerManifestFactoryTest extends AbstractTlvManifestTest {

    private TlvContainerManifestFactory factory = new TlvContainerManifestFactory();
    private Pair<String, ContainerAnnotation> mockedAnnotationPair;
    private Pair<String, TlvDataFilesManifest> mockedDataManifestPair;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockedAnnotationPair = Pair.of(MOCK_URI, mockAnnotation);
        mockedDataManifestPair = Pair.of(TEST_FILE_NAME_TEST_TXT, mockDataManifest);
    }

    @Test
    public void testCreateSingleAnnotationManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document manifest");
        factory.createSingleAnnotationManifest(null, mockedAnnotationPair);
    }

    @Test
    public void testCreateSingleAnnotationManifestWithoutAnnotation_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotation");
        factory.createSingleAnnotationManifest(mockedDataManifestPair, null);
    }

    @Test
    public void testCreateSingleAnnotationManifest() throws Exception {
        TlvSingleAnnotationManifest singleAnnotationManifest = factory.createSingleAnnotationManifest(mockedDataManifestPair, mockedAnnotationPair);
        assertNotNull(singleAnnotationManifest);
        assertNotNull(singleAnnotationManifest.getAnnotationReference());
        assertNotNull(singleAnnotationManifest.getDataManifestReference());
    }

//    @Test
//    public void testCreateAnnotationsManifestWithoutSingleAnnotationManifests_ThrowsIllegalArgumentException() throws Exception {
//        expectedException.expect(NullPointerException.class);
//        expectedException.expectMessage("Annotation");
//        factory.createAnnotationsManifest();
//    }
//
//    @Test
//    public void testCreateAnnotationsManifestOK() throws Exception {
//        Map<ContainerAnnotation, TlvSingleAnnotationManifest> annotationManifests = new HashMap();
//        annotationManifests.put(mockAnnotation, mockSingleAnnotationManifest);
//        TlvAnnotationsManifest annotationsManifest = factory.createAnnotationsManifest(annotationManifests, "Non-important-for-test");
//
//        assertNotNull("Manifest was not created", manifest);
//    }

    @Test
    public void testCreateDataFilesManifestWithEmptyDataFiles_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files list must not be empty");
        factory.createDataFilesManifest(new ArrayList<ContainerDocument>());
    }

    @Test
    public void testCreateDataFilesManifestWithoutDataFiles_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document list must be present");
        factory.createDataFilesManifest(null);
    }

    @Test
    public void testCreateDataFilesManifestOK() throws Exception {
        TlvDataFilesManifest dataFilesManifest = factory.createDataFilesManifest(asList(TEST_DOCUMENT_HELLO_TEXT));
        assertNotNull("Manifest was not created", dataFilesManifest);
        assertEquals(1, dataFilesManifest.getDataFileReferences().size());
    }

//    @Test
//    public void testCreateSignatureManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
//        expectedException.expect(NullPointerException.class);
//        expectedException.expectMessage("kala");
//        factory.createSignatureManifest(null, mockAnnotationsManifest, "Non-important-for-test", "signature.ksig");
//    }

//    @Test
//    public void testCreateSignatureManifestWithoutAnnotationsManifest_ThrowsNullPointerException() throws Exception {
//        expectedException.expect(NullPointerException.class);
//        factory.createSignatureManifest(mockDataManifest, null, "Non-important-for-test", "signature.ksig");
//    }
//
//    @Test
//    public void testCreateSignatureManifestOK() throws Exception {
//        TlvSignatureManifest signatureManifest = factory.createSignatureManifest(mockDataManifest, mockAnnotationsManifest, "Non-important-for-test", "signature.ksig");
//
//        assertNotNull("Manifest was not created", manifest);
//    }


    @Test
    public void testGetManifestFactoryType() throws Exception {
        TlvManifestFactoryType type = factory.getManifestFactoryType();
        assertNotNull(type);
        assertNotNull(type.getManifestFileExtension());
        assertNotNull(type.getName());
    }

}