package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TlvManifestTest extends AbstractTlvManifestTest {

    private TLVElement annotationsManifestReference;
    private TLVElement signatureReference;
    private TLVElement documentsManifestReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.annotationsManifestReference = createReference(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, ANNOTATIONS_MANIFEST_URI, ANNOTATIONS_MANIFEST_TYPE, dataHash);
        this.documentsManifestReference = createReference(DOCUMENTS_MANIFEST_REFERENCE_TYPE, DOCUMENTS_MANIFEST_URI, MIME_TYPE_APPLICATION_TXT, dataHash);
        this.signatureReference = createReference(SIGNATURE_REFERENCE_TYPE, SIGNATURE_URI, SIGNATURE_TYPE, null);
    }

    @Test
    public void testCreateManifest() throws Exception {
        Pair<String, TlvDocumentsManifest> documentsManifest = Pair.of(DOCUMENTS_MANIFEST_URI, mockDocumentsManifest);
        Pair<String, TlvAnnotationsManifest> annotationsManifest = Pair.of(ANNOTATIONS_MANIFEST_URI, mockAnnotationsManifest);
        Pair<String, String> signatureReference = Pair.of(SIGNATURE_URI, SIGNATURE_TYPE);
        TlvManifest manifest = new TlvManifest(documentsManifest, annotationsManifest, signatureReference);

        assertArrayEquals(SIGNATURE_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getDocumentsManifestReference());
        assertNotNull(manifest.getAnnotationsManifestReference());
        assertNotNull(manifest.getSignatureReference());
        assertEquals(DOCUMENTS_MANIFEST_URI, manifest.getDocumentsManifestReference().getUri());
        assertEquals(ANNOTATIONS_MANIFEST_URI, manifest.getAnnotationsManifestReference().getUri());
        assertEquals(SIGNATURE_URI, manifest.getSignatureReference().getUri());
    }

    @Test
    public void testReadManifest() throws Exception {
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), documentsManifestReference.getEncoded(), signatureReference.getEncoded());

        TlvManifest manifest = new TlvManifest(new ByteArrayInputStream(manifestBytes));
        assertArrayEquals(SIGNATURE_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getDocumentsManifestReference());
        assertNotNull(manifest.getAnnotationsManifestReference());
        assertNotNull(manifest.getSignatureReference());
        assertEquals(DOCUMENTS_MANIFEST_URI, manifest.getDocumentsManifestReference().getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, manifest.getDocumentsManifestReference().getMimeType());
        assertEquals(ANNOTATIONS_MANIFEST_URI, manifest.getAnnotationsManifestReference().getUri());
        assertEquals(ANNOTATIONS_MANIFEST_TYPE, manifest.getAnnotationsManifestReference().getMimeType());
        assertEquals(SIGNATURE_URI, manifest.getSignatureReference().getUri());
        assertEquals(SIGNATURE_MIME_TYPE, manifest.getSignatureReference().getType());
    }

    @Test
    public void testReadManifestWithoutSingleAnnotationManifestReference_ThrowsInvalidManifestException() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Annotations manifest reference is mandatory");
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, documentsManifestReference.getEncoded(), signatureReference.getEncoded());
        new TlvManifest(new ByteArrayInputStream(manifestBytes));
    }

    @Test
    public void testReadManifestWithoutSignatureManifestReference_ThrowsInvalidManifestException() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Signature reference is mandatory");
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), documentsManifestReference.getEncoded());
        new TlvManifest(new ByteArrayInputStream(manifestBytes));
    }

    @Test
    public void testReadManifestWithoutDocumentsManifestReference_ThrowsInvalidManifestException() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Documents manifest reference is mandatory");
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), signatureReference.getEncoded());
        new TlvManifest(new ByteArrayInputStream(manifestBytes));
    }

}