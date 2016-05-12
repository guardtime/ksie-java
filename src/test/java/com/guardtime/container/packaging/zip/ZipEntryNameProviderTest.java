package com.guardtime.container.packaging.zip;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZipEntryNameProviderTest {

    private static final int PARSED_MANIFEST_INDEX = 5;
    private static final int PARSED_ANNOTATION_INDEX = 17;
    private static final String META_INF = "META-INF/";
    private ZipEntryNameProvider nameProvider = new ZipEntryNameProvider("tlv", "ksi");
    private ZipEntryNameProvider preInitializedNameProvider = new ZipEntryNameProvider("tlv", "ksi", PARSED_MANIFEST_INDEX, PARSED_ANNOTATION_INDEX);

    @Test
    public void testNextDocumentsManifestName() throws Exception {
        String documentsManifestName = nameProvider.nextDocumentsManifestName();
        assertEquals(META_INF + "datamanifest1.tlv", documentsManifestName);
        assertEquals(nameProvider.documentsManifestIndex, 1);
    }

    @Test
    public void testNextManifestName() throws Exception {
        String manifestName = nameProvider.nextManifestName();
        assertEquals(META_INF + "manifest1.tlv", manifestName);
        assertEquals(nameProvider.manifestIndex, 1);
    }

    @Test
    public void testNextSignatureName() throws Exception {
        String name = nameProvider.nextSignatureName();
        assertEquals(META_INF + "signature1.ksi", name);
        assertEquals(nameProvider.signatureIndex, 1);
    }

    @Test
    public void testNextManifestNamePreInitialized() throws Exception {
        String manifestName = preInitializedNameProvider.nextManifestName();
        int parsedManifestIndex = PARSED_MANIFEST_INDEX + 1;
        assertEquals(META_INF + "manifest" + parsedManifestIndex + ".tlv", manifestName);
        assertEquals(preInitializedNameProvider.manifestIndex, parsedManifestIndex);
    }



    @Test
    public void testNextAnnotationNamePreInitialized() throws Exception {
        String name = preInitializedNameProvider.nextAnnotationDataFileName();
        int parsedAnnotationIndex = PARSED_ANNOTATION_INDEX + 1;
        assertEquals(META_INF + "annotation" + parsedAnnotationIndex + ".dat", name);
        assertEquals(preInitializedNameProvider.annotationIndex, parsedAnnotationIndex);
    }

}