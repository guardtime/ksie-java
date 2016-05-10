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

public class TlvSignatureManifestTest extends AbstractTlvManifestTest {

    private TLVElement annotationsManifestReference;
    private TLVElement signatureReference;
    private TLVElement dataFilesReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.annotationsManifestReference = createReference(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, ANNOTATIONS_MANIFEST_URI, ANNOTATIONS_MANIFEST_TYPE, dataHash);
        this.dataFilesReference = createReference(DATA_MANIFEST_REFERENCE_TYPE, DATAFILES_MANIFEST_URI, MIME_TYPE_APPLICATION_TXT, dataHash);
        this.signatureReference = createReference(SIGNATURE_REFERENCE_TYPE, SIGNATURE_URI, SIGNATURE_TYPE, null);
    }

    @Test
    public void testCreateManifest() throws Exception {
        Pair<String, TlvDataFilesManifest> dataManifest = Pair.of(DATAFILES_MANIFEST_URI, mockDataManifest);
        Pair<String, TlvAnnotationsManifest> annotationsManifest = Pair.of(ANNOTATIONS_MANIFEST_URI, mockAnnotationsManifest);
        Pair<String, String> signatureReference = Pair.of(SIGNATURE_URI, SIGNATURE_TYPE);
        TlvSignatureManifest signatureManifest = new TlvSignatureManifest(dataManifest, annotationsManifest, signatureReference);

        assertArrayEquals(SIGNATURE_MANIFEST_MAGIC, signatureManifest.getMagic());
        assertNotNull(signatureManifest.getDataFilesManifestReference());
        assertNotNull(signatureManifest.getAnnotationsManifestReference());
        assertNotNull(signatureManifest.getSignatureReference());
        assertEquals(DATAFILES_MANIFEST_URI, signatureManifest.getDataFilesManifestReference().getUri());
        assertEquals(ANNOTATIONS_MANIFEST_URI, signatureManifest.getAnnotationsManifestReference().getUri());
        assertEquals(SIGNATURE_URI, signatureManifest.getSignatureReference().getUri());
    }

    @Test
    public void testReadManifest() throws Exception {
        byte[] manifestBytes = join(SIGNATURE_MANIFEST_MAGIC, annotationsManifestReference.getEncoded(), dataFilesReference.getEncoded(), signatureReference.getEncoded());

        TlvSignatureManifest signatureManifest = new TlvSignatureManifest(new ByteArrayInputStream(manifestBytes));
        assertArrayEquals(SIGNATURE_MANIFEST_MAGIC, signatureManifest.getMagic());
        assertNotNull(signatureManifest.getDataFilesManifestReference());
        assertNotNull(signatureManifest.getAnnotationsManifestReference());
        assertNotNull(signatureManifest.getSignatureReference());
        assertEquals(DATAFILES_MANIFEST_URI, signatureManifest.getDataFilesManifestReference().getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, signatureManifest.getDataFilesManifestReference().getMimeType());
        assertEquals(ANNOTATIONS_MANIFEST_URI, signatureManifest.getAnnotationsManifestReference().getUri());
        assertEquals(ANNOTATIONS_MANIFEST_TYPE, signatureManifest.getAnnotationsManifestReference().getMimeType());
        assertEquals(SIGNATURE_URI, signatureManifest.getSignatureReference().getUri());
        assertEquals(SIGNATURE_TYPE, signatureManifest.getSignatureReference().getType());
    }

    @Test
    public void testReadManifestWithoutSingleAnnotationManifestReference() throws Exception {
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