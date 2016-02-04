package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.LinkedList;

public class TlvDataFilesManifestTest extends AbstractTlvManifestTest {
    private TlvDataFilesManifest manifest;

    @Before
    public void setUpManifest() throws TLVParserException, BlockChainContainerException {
        LinkedList<ContainerDocument> documents = new LinkedList<>();
        documents.add(mockDocument);
        this.manifest = new TlvDataFilesManifest(documents, "Non-important-for-test");
    }

    @Test
    public void testInputStreamTlvElementExistence() throws Exception {
        InputStream is = manifest.getInputStream();
        testMagic(is, DATA_FILES_MANIFEST_MAGIC);

        testTlvElement(new TLVInputStream(is), DATA_FILE_REFERENCE_TYPE);
    }

}