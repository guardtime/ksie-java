package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvSignatureManifestTest extends AbstractTlvManifestTest {
    private TlvSignatureManifest manifest;

    @Before
    public void setUpManifest() throws TLVParserException, BlockChainContainerException {
        List<TLVElement> elements = new LinkedList<>();
        elements.add(TlvReferenceElementFactory.createDataManifestReferenceTlvElement(mockDataManifest));
        elements.add(TlvReferenceElementFactory.createSignatureReferenceTlvElement("META-INF/signature1.ksig"));
        elements.add(TlvReferenceElementFactory.createAnnotationsManifestReferenceTlvElement(mockAnnotationsManifest));
        this.manifest = new TlvSignatureManifest(elements, "Non-important-for-test");
    }

    @Test
    public void testInputStreamTlvElementExistence() throws Exception {
        InputStream is = manifest.getInputStream();
        testMagic(is, SIGNATURE_MANIFEST_MAGIC);

        TLVInputStream tlvInputStream = new TLVInputStream(is);
        testTlvElement(tlvInputStream, DATA_MANIFEST_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, SIGNATURE_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, ANNOTATIONS_MANIFEST_REFERENCE_TYPE);

    }
}