package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class TlvSingleAnnotationManifestTest extends AbstractTlvManifestTest {

    private TLVElement annotationReference;
    private TLVElement dataFilesReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.annotationReference = createAnnotationReferenceElement();
        this.dataFilesReference = createReference(DATA_MANIFEST_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHash);
    }

    @Test
    public void testCreateSingleAnnotationManifest() throws Exception {
        TlvSingleAnnotationManifest annotationManifest = new TlvSingleAnnotationManifest(Pair.of(SINGLE_ANNOTATION_MANIFEST_URI, mockAnnotation), Pair.of(MOCK_URI, mockDataManifest));

        assertNotNull(annotationManifest.getAnnotationReference());
        assertNotNull(annotationManifest.getDataManifestReference());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, annotationManifest.getAnnotationReference().getDomain());
        assertEquals(SINGLE_ANNOTATION_MANIFEST_URI, annotationManifest.getAnnotationReference().getUri());
        assertEquals(dataHash, annotationManifest.getAnnotationReference().getHash());
        assertEquals(MOCK_URI, annotationManifest.getDataManifestReference().getUri());
        assertEquals(DATA_MANIFEST_TYPE, annotationManifest.getDataManifestReference().getMimeType());
        assertEquals(dataHash, annotationManifest.getAnnotationReference().getHash());
    }

    @Test
    public void testReadSingleAnnotationManifest() throws Exception {
        byte[] bytes = join(SINGLE_ANNOTATION_MANIFEST_MAGIC, annotationReference.getEncoded(), dataFilesReference.getEncoded());
        TlvSingleAnnotationManifest annotationInfoManifest = new TlvSingleAnnotationManifest(new ByteArrayInputStream(bytes));

        assertArrayEquals(SINGLE_ANNOTATION_MANIFEST_MAGIC, annotationInfoManifest.getMagic());
        assertNotNull(annotationInfoManifest.getDataManifestReference());
        assertNotNull(annotationInfoManifest.getAnnotationReference());
    }

    @Test
    public void testReadSingleAnnotationManifestWithoutDataReference() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Data manifest reference is mandatory manifest element");
        byte[] bytes = join(SINGLE_ANNOTATION_MANIFEST_MAGIC, annotationReference.getEncoded());
        new TlvSingleAnnotationManifest(new ByteArrayInputStream(bytes));
    }

    @Test
    public void testReadSingleAnnotationManifestWithoutAnnotationReference() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Annotation reference is mandatory manifest element");
        byte[] bytes = join(SINGLE_ANNOTATION_MANIFEST_MAGIC, dataFilesReference.getEncoded());
        new TlvSingleAnnotationManifest(new ByteArrayInputStream(bytes));
    }

}