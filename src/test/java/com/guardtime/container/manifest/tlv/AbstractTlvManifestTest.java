package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.datafile.ContainerDocument;
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
    private DataHash dataHash;

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.dataHash = new DataHash(HashAlgorithm.SHA2_256, "12345678901234567890123456789012".getBytes());
        when(mockDataManifest.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // TODO: Maybe give valid TLV stream ?
        when(mockAnnotationsManifest.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // TODO: Maybe give valid TLV stream ?

        when(mockAnnotation.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockAnnotation.getAnnotationType()).thenReturn(ContainerAnnotationType.NON_REMOVABLE);
        when(mockAnnotationInfoManifest.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // TODO: Maybe give valid TLV stream ?
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
}
