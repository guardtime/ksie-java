package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TlvAnnotationsManifestTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationsManifest() throws Exception {
        Map<ContainerAnnotation, Pair<String, TlvAnnotationInfoManifest>> annotationManifest = new HashMap<>();
        annotationManifest.put(mockAnnotation, Pair.of(MOCK_URI, mockAnnotationInfoManifest));
        TlvAnnotationsManifest manifest = new TlvAnnotationsManifest(annotationManifest);
        assertArrayEquals(ANNOTATIONS_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getAnnotationManifestReferences());
        assertNotNull(manifest.getAnnotationManifestReferences().get(0));
    }

    @Test
    public void testReadAnnotationsManifest() throws Exception {
        TLVElement annotationsInfoReference = createReference(ANNOTATION_INFO_REFERENCE_TYPE, MOCK_URI, DATA_FILE_MIME_TYPE, dataHash);
        byte[] bytes = join(ANNOTATIONS_MANIFEST_MAGIC, annotationsInfoReference.getEncoded());

        TlvAnnotationsManifest manifest = new TlvAnnotationsManifest(new ByteArrayInputStream(bytes));
        assertArrayEquals(ANNOTATIONS_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getAnnotationManifestReferences());
        assertEquals(1, manifest.getAnnotationManifestReferences().size());
        FileReference annotationsReference = manifest.getAnnotationManifestReferences().get(0);
        assertEquals(MOCK_URI, annotationsReference.getUri());
        assertEquals(DATA_FILE_MIME_TYPE, annotationsReference.getMimeType());
        assertEquals(dataHash, annotationsReference.getHash());
    }

    @Test
    public void testReadAnnotationsManifestUsingInvalidMagicBytes() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Invalid magic for manifest type");
        new TlvAnnotationsManifest(new ByteArrayInputStream(DATA_FILES_MANIFEST_MAGIC));
    }

    @Test
    public void testReadAnnotationsManifestWithoutAnnotationReferences() throws Exception {
        TlvAnnotationsManifest manifest = new TlvAnnotationsManifest(new ByteArrayInputStream(ANNOTATIONS_MANIFEST_MAGIC));
        assertArrayEquals(ANNOTATIONS_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getAnnotationManifestReferences());
        assertTrue(manifest.getAnnotationManifestReferences().isEmpty());
    }

}