package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Pair;

import org.junit.Test;

public class TlvContainerManifestFactoryTest extends AbstractTlvManifestTest {

    private TlvContainerManifestFactory factory = new TlvContainerManifestFactory();

    @Test
    public void testCreateAnnotationInfoManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        factory.createAnnotationManifest(null, Pair.of("",mockAnnotation));
    }

    @Test
    public void testCreateAnnotationInfoManifestWithoutAnnotation_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        factory.createAnnotationManifest(Pair.of(DATA_FILE_NAME,mockDataManifest), null);
    }

//    @Test
//    public void testCreateAnnotationInfoManifest() throws Exception {
//        TlvAnnotationInfoManifest manifest = factory.createAnnotationManifest(Pair.of(DATA_FILE_NAME,mockDataManifest), mockAnnotation);
//
//        assertEquals(manifest.get)
//    }
//
//    @Test
//    public void testCreateAnnotationsManifestWithoutAnnotationInfoManifests_ThrowsIllegalArgumentException() throws Exception {
//        expectedException.expect(IllegalArgumentException.class);
//        factory.createAnnotationsManifest(new HashMap<ContainerAnnotation, TlvAnnotationInfoManifest>(), "Non-important-for-test");
//    }
//
//    @Test
//    public void testCreateAnnotationsManifestOK() throws Exception {
//        Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests = new HashMap();
//        annotationManifests.put(mockAnnotation, mockAnnotationInfoManifest);
//        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(annotationManifests, "Non-important-for-test");
//
//        assertNotNull("Manifest was not created", manifest);
//    }

//    @Test
//    public void testCreateDataFilesManifestWithoutDataFiles_ThrowsIllegalArgumentException() throws Exception {
//        expectedException.expect(IllegalArgumentException.class);
//        factory.createDataFilesManifest(new LinkedList<ContainerDocument>(), "Non-important-for-test");
//    }

//    @Test
//    public void testCreateDataFilesManifestOK() throws Exception {
//        LinkedList<ContainerDocument> documents = new LinkedList<>();
//        documents.add(mockDocument);
//        TlvDataFilesManifest manifest = factory.createDataFilesManifest(documents, "Non-important-for-test");
//
//        assertNotNull("Manifest was not created", manifest);
//    }

//    @Test
//    public void testCreateSignatureManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
//        expectedException.expect(NullPointerException.class);
//        factory.createSignatureManifest(null, mockAnnotationsManifest, "Non-important-for-test", "signature.ksig");
//    }
//
//    @Test
//    public void testCreateSignatureManifestWithoutAnnotationsManifest_ThrowsNullPointerException() throws Exception {
//        expectedException.expect(NullPointerException.class);
//        factory.createSignatureManifest(mockDataManifest, null, "Non-important-for-test", "signature.ksig");
//    }
//
//    @Test
//    public void testCreateSignatureManifestOK() throws Exception {
//        TlvSignatureManifest manifest = factory.createSignatureManifest(mockDataManifest, mockAnnotationsManifest, "Non-important-for-test", "signature.ksig");
//
//        assertNotNull("Manifest was not created", manifest);
//    }

}