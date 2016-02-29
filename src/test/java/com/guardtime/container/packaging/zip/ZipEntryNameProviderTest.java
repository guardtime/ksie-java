package com.guardtime.container.packaging.zip;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZipEntryNameProviderTest {

    private ZipEntryNameProvider nameProvider = new ZipEntryNameProvider("tlv", "ksi");

    @Test
    public void testNextDataManifestName() throws Exception {
        String dataManifestName = nameProvider.nextDataManifestName();
        assertEquals("/META-INF/datamanifest1.tlv", dataManifestName);
        assertEquals(nameProvider.dataManifestIndex, 1);
    }

    @Test
    public void testNextManifestName() throws Exception {
        String manifestName = nameProvider.nextManifestName();
        assertEquals("/META-INF/manifest1.tlv", manifestName);
        assertEquals(nameProvider.manifestIndex, 1);
    }

    @Test
    public void testNextSignatureName() throws Exception {
        String name = nameProvider.nextSignatureName();
        assertEquals("/META-INF/signature1.ksi", name);
        assertEquals(nameProvider.signatureIndex, 1);
    }

}