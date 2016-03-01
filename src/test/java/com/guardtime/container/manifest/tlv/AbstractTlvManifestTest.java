package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.manifest.AnnotationReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static com.guardtime.container.util.Util.hash;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class AbstractTlvManifestTest {

    // TODO: Review constants when spec is final
    protected static final int DATA_MANIFEST_REFERENCE_TYPE = 0xb01;
    protected static final int ANNOTATIONS_MANIFEST_REFERENCE_TYPE = 0xb02;
    protected static final int DATA_FILE_REFERENCE_TYPE = 0xb03;
    protected static final int ANNOTATION_INFO_REFERENCE_TYPE = 0xb04;
    protected static final int ANNOTATION_REFERENCE_TYPE = 0xb05;
    protected static final int SIGNATURE_REFERENCE_TYPE = 0xb06;
    protected static final byte[] ANNOTATION_INFO_MANIFEST_MAGIC = "KSIEANNT".getBytes();
    protected static final byte[] ANNOTATIONS_MANIFEST_MAGIC = "KSIEANMF".getBytes();
    protected static final byte[] DATA_FILES_MANIFEST_MAGIC = "KSIEDAMF".getBytes();
    protected static final byte[] SIGNATURE_MANIFEST_MAGIC = "KSIEMFST".getBytes();
    protected static final String ANNOTATION_MANIFEST_TYPE = "ksie10/annotmanifest";
    protected static final String ANNOTATION_CONTENT = "AnnotationTestContent";
    protected static final String ANNOTATION_DOMAIN = "com.guardtime";
    protected static final String MOCK_URI = "/mock/mock";
    protected static final byte[] DATA_FILE_CONTENT = "Test".getBytes();
    protected static final String DATA_FILE_MIME_TYPE = "text";
    protected static final String DATA_FILE_NAME = "hello.txt";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected TlvDataFilesManifest mockDataManifest;

    @Mock
    protected TlvAnnotationsManifest mockAnnotationsManifest;

    @Mock
    protected ContainerAnnotation mockAnnotation;

    @Mock
    protected TlvAnnotationInfoManifest mockAnnotationInfoManifest;

    @Mock
    protected ContainerDocument mockDocument;

    @Mock
    private AnnotationReference mockAnnotationReference;

    protected DataHash dataHash;
    protected ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(DATA_FILE_CONTENT), DATA_FILE_MIME_TYPE, DATA_FILE_NAME);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.dataHash = hash(new ByteArrayInputStream(DATA_FILE_CONTENT), HashAlgorithm.SHA2_256);
        when(mockDataManifest.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // TODO: Maybe give valid TLV stream ?
        when(mockAnnotationsManifest.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // TODO: Maybe give valid TLV stream ?

        when(mockAnnotation.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockAnnotation.getAnnotationType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE);
        when(mockAnnotation.getDomain()).thenReturn(ANNOTATION_DOMAIN);

        when(mockAnnotationInfoManifest.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // TODO: Maybe give valid TLV stream ?
        when(mockAnnotationInfoManifest.getAnnotationReference()).thenReturn(mockAnnotationReference);

        when(mockAnnotationReference.getDomain()).thenReturn(ANNOTATION_DOMAIN);
        when(mockAnnotationReference.getUri()).thenReturn(MOCK_URI);
        when(mockAnnotationReference.getHash()).thenReturn(dataHash);


        when(mockDocument.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockDocument.getFileName()).thenReturn("RandomFileIsAwesome.txt");
        when(mockDocument.getMimeType()).thenReturn("application/text");
    }

    protected void testMagic(InputStream stream, byte[] magic) throws Exception {
        // TODO: Improve this. When final spec is out fix the byte array size
        byte[] data = new byte[8];
        stream.read(data); // magic
        assertTrue("Magic doesn't match", Arrays.equals(magic, data));
    }

    protected void testTlvElement(TLVInputStream stream, int type) throws Exception {
        TLVElement element = stream.readElement();
        assertEquals(type, element.getType());
    }

    protected TLVElement createReference(int referenceType, String referenceUri, String referenceMime, DataHash dataHash) throws Exception {
        TLVElement reference = new TLVElement(false, false, referenceType);
        if (referenceUri != null) {
            TLVElement uri = new TLVElement(false, false, 0x01);
            uri.setStringContent(referenceUri);
            reference.addChildElement(uri);
        }
        if (referenceMime != null) {
            TLVElement mime = new TLVElement(false, false, 0x03);
            mime.setStringContent(referenceMime);
            reference.addChildElement(mime);
        }
        if (dataHash != null) {
            TLVElement hash = new TLVElement(false, false, 0x02);
            hash.setDataHashContent(dataHash);
            reference.addChildElement(hash);
        }
        return reference;
    }

}
