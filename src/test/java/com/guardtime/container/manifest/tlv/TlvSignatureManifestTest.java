package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.util.Util;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TlvSignatureManifestTest extends AbstractTlvManifestTest {

    private static final String MOCK_DATAFILES_MANIFEST_URI = "/mock/datafiles";
    private static final String MOCK_ANNOTATIONS_MANIFEST_URI = "/mock/annotationsmanifest";
    private static final String MOCK_SIGNATURE_URI = "/mock/signature";

    private TLVElement annotationsManifestReference;
    private TLVElement signatureReference;
    private TLVElement dataFilesReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.annotationsManifestReference = createReference(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, MOCK_ANNOTATIONS_MANIFEST_URI, ANNOTATION_MANIFEST_TYPE, dataHash);
        this.dataFilesReference = createReference(DATA_MANIFEST_REFERENCE_TYPE, MOCK_DATAFILES_MANIFEST_URI, DATA_FILE_MIME_TYPE, dataHash);
        this.signatureReference = createReference(SIGNATURE_REFERENCE_TYPE, MOCK_SIGNATURE_URI, SIGNATURE_TYPE, null);
    }

    @Test
    public void testCreateManifest() throws Exception {
        Pair<String, TlvDataFilesManifest> dataManifest = Pair.of(MOCK_DATAFILES_MANIFEST_URI, mockDataManifest);
        Pair<String, TlvAnnotationsManifest> annotationsManifest = Pair.of(MOCK_ANNOTATIONS_MANIFEST_URI, mockAnnotationsManifest);
        Pair<String, String> signatureReference = Pair.of(MOCK_SIGNATURE_URI, SIGNATURE_TYPE);
        TlvSignatureManifest manifest = new TlvSignatureManifest(dataManifest, annotationsManifest, signatureReference);

        assertArrayEquals(SIGNATURE_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getDataFilesReference());
        assertNotNull(manifest.getAnnotationsManifestReference());
        assertNotNull(manifest.getSignatureReference());
        assertEquals(MOCK_DATAFILES_MANIFEST_URI, manifest.getDataFilesReference().getUri());
        assertEquals(MOCK_ANNOTATIONS_MANIFEST_URI, manifest.getAnnotationsManifestReference().getUri());
        assertEquals(MOCK_SIGNATURE_URI, manifest.getSignatureReference().getUri());
    }

    @Test
    public void testReadManifest() throws Exception {
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), dataFilesReference.getEncoded(), signatureReference.getEncoded());

        TlvSignatureManifest manifest = new TlvSignatureManifest(new ByteArrayInputStream(manifestBytes));
        assertArrayEquals(SIGNATURE_MANIFEST_MAGIC, manifest.getMagic());
        assertNotNull(manifest.getDataFilesReference());
        assertNotNull(manifest.getAnnotationsManifestReference());
        assertNotNull(manifest.getSignatureReference());
        assertEquals(MOCK_DATAFILES_MANIFEST_URI, manifest.getDataFilesReference().getUri());
        assertEquals(DATA_FILE_MIME_TYPE, manifest.getDataFilesReference().getMimeType());
        assertEquals(MOCK_ANNOTATIONS_MANIFEST_URI, manifest.getAnnotationsManifestReference().getUri());
        assertEquals(ANNOTATION_MANIFEST_TYPE, manifest.getAnnotationsManifestReference().getMimeType());
        assertEquals(MOCK_SIGNATURE_URI, manifest.getSignatureReference().getUri());
        assertEquals(SIGNATURE_TYPE, manifest.getSignatureReference().getType());
    }

    @Test
    public void testReadManifestWithoutAnnotationManifestReference() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Annotations manifest reference is mandatory");
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, dataFilesReference.getEncoded(), signatureReference.getEncoded());
        new TlvSignatureManifest(new ByteArrayInputStream(manifestBytes));
    }

    @Test
    public void testReadManifestWithoutSignatureManifestReference() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Signature manifest reference is mandatory");
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), dataFilesReference.getEncoded());
        new TlvSignatureManifest(new ByteArrayInputStream(manifestBytes));
    }

    @Test
    public void testReadManifestWithoutDataFilesManifestReference() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Data files manifest reference is mandatory");
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), signatureReference.getEncoded());
        new TlvSignatureManifest(new ByteArrayInputStream(manifestBytes));
    }

}