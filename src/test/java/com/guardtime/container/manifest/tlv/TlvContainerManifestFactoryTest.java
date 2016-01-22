package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TlvContainerManifestFactoryTest {
    // TODO: Review constants when spec is final
    private static final int SIGNATURE_REFERENCE_TYPE = 0xb06;
    private static final int DATA_MANIFEST_REFERENCE_TYPE = 0xb01;
    private static final int ANNOTATIONS_MANIFEST_REFERENCE_TYPE = 0xb02;
    private static final int DATA_FILE_REFERENCE_TYPE = 0xb03;
    private static final int ANNOTATION_INFO_REFERENCE_TYPE = 0xb04;
    private static final int ANNOTATION_REFERENCE_TYPE = 0xb05;
    private static final byte[] ANNOTATION_INFO_MANIFEST_MAGIC = "KSIEANNT".getBytes();
    private static final byte[] ANNOTATIONS_MANIFEST_MAGIC = "KSIEANMF".getBytes();
    private static final byte[] DATA_FILES_MANIFEST_MAGIC = "KSIEDAMF".getBytes();
    private static final byte[] SIGNATURE_MANIFEST_MAGIC = "KSIEMFST".getBytes();
    private DataHash dataHash;

    @Mock
    private TlvDataFilesManifest mockDataManifest;

    @Mock
    private TlvAnnotationsManifest mockAnnotationsManifest;

    @Mock
    private ContainerAnnotation mockAnnotation;

    private TlvContainerManifestFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // TODO: Code cleanup
        this.dataHash = new DataHash(HashAlgorithm.SHA2_256, "12345678901234567890123456789012".getBytes());
        when(mockDataManifest.getInputStream()).thenReturn(new ByteArrayInputStream("dataManifestStuffGoesHereOk".getBytes()));
        when(mockAnnotationsManifest.getInputStream()).thenReturn(new ByteArrayInputStream("annotationsManifestStuffGoesHereOk".getBytes()));
        when(mockAnnotation.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockAnnotation.getMimeType()).thenReturn("non-removable");
        factory = new TlvContainerManifestFactory();
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAnnotationInfoManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
        factory.createAnnotationManifest(null, mockAnnotation);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAnnotationInfoManifestWithoutAnnotation_ThrowsNullPointerException() throws Exception {
        factory.createAnnotationManifest(mockDataManifest, null);
    }

    @Test
    public void testCreateAnnotationInfoManifest() throws Exception {
        TlvAnnotationInfoManifest manifest = factory.createAnnotationManifest(mockDataManifest, mockAnnotation);

        assertNotNull("Manifest was not created", manifest);

        InputStream is = manifest.getInputStream();
        testMagic(is, ANNOTATION_INFO_MANIFEST_MAGIC);

        TLVInputStream tlvInputStream = new TLVInputStream(is);
        testTlvElement(tlvInputStream, DATA_MANIFEST_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, ANNOTATION_REFERENCE_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAnnotationsManifestWithoutAnnotationInfoManifests_ThrowsIllegalArgumentException() throws Exception {
        factory.createAnnotationsManifest(new LinkedList<AnnotationInfoManifest>(), "Non-important-for-test");
    }

    @Test
    public void testCreateAnnotationsManifest() throws Exception {
        LinkedList<TlvAnnotationInfoManifest> annotations = new LinkedList<>();
        TlvAnnotationInfoManifest mockAnnotationInfoManifest = Mockito.mock(TlvAnnotationInfoManifest.class);
        when(mockAnnotationInfoManifest.getInputStream()).thenReturn(new ByteArrayInputStream("dataManifestStuffGoesHereOk".getBytes()));
        when(mockAnnotationInfoManifest.getAnnotation()).thenReturn(mockAnnotation);
        annotations.add(mockAnnotationInfoManifest);
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(annotations, "Non-important-for-test");

        assertNotNull("Manifest was not created", manifest);

        InputStream is = manifest.getInputStream();
        testMagic(is, ANNOTATIONS_MANIFEST_MAGIC);

        testTlvElement(new TLVInputStream(is), ANNOTATION_INFO_REFERENCE_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDataFilesManifestWithoutDataFiles_ThrowsIllegalArgumentException() throws Exception {
        factory.createDataFilesManifest(new LinkedList<ContainerDocument>(), "Non-important-for-test");
    }

    @Test
    public void testCreateDataFilesManifest() throws Exception {
        LinkedList<ContainerDocument> documents = new LinkedList<>();
        ContainerDocument mockDocument = Mockito.mock(ContainerDocument.class);
        when(mockDocument.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockDocument.getFileName()).thenReturn("RandomFileIsAwesome.txt");
        when(mockDocument.getMimeType()).thenReturn("application/text");
        documents.add(mockDocument);
        TlvDataFilesManifest manifest = factory.createDataFilesManifest(documents, "Non-important-for-test");

        assertNotNull("Manifest was not created", manifest);

        InputStream is = manifest.getInputStream();
        testMagic(is, DATA_FILES_MANIFEST_MAGIC);

        testTlvElement(new TLVInputStream(is), DATA_FILE_REFERENCE_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateSignatureManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
        factory.createSignatureManifest(null, mockAnnotationsManifest, "Non-important-for-test");
    }

    @Test(expected = NullPointerException.class)
    public void testCreateSignatureManifestWithoutAnnotationsManifest_ThrowsNullPointerException() throws Exception {
        factory.createSignatureManifest(mockDataManifest, null, "Non-important-for-test");
    }

    @Test
    public void testCreateSignatureManifest() throws Exception {
        TlvSignatureManifest manifest = factory.createSignatureManifest(mockDataManifest, mockAnnotationsManifest, "Non-important-for-test");

        assertNotNull("Manifest was not created", manifest);

        InputStream is = manifest.getInputStream();
        testMagic(is, SIGNATURE_MANIFEST_MAGIC);

        TLVInputStream tlvInputStream = new TLVInputStream(is);
        testTlvElement(tlvInputStream, DATA_MANIFEST_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, SIGNATURE_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, ANNOTATIONS_MANIFEST_REFERENCE_TYPE);
    }


    private void testMagic(InputStream stream, byte[] magic) throws Exception {
        // TODO: Improve this. When final spec is out fix the byte array size
        byte[] data = new byte[8];
        stream.read(data); // magic
        assertTrue("Magic doesn't match", Arrays.equals(magic, data));
    }

    private void testTlvElement(TLVInputStream stream, int type) throws Exception {
        TLVElement element = stream.readElement();
        assertEquals(type, element.getType());
    }

}