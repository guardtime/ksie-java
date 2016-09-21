package com.guardtime.container.packaging;

import com.guardtime.container.indexing.IncrementingIndexProvider;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class EntryNameProviderTest {

    private static final String META_INF = "META-INF/";
    private final IncrementingIndexProvider indexProvider = spy(new IncrementingIndexProvider());
    private EntryNameProvider nameProvider = new EntryNameProvider("tlv", "ksi", indexProvider);

    @Test
    public void testNextDocumentsManifestName() throws Exception {
        String documentsManifestName = nameProvider.nextDocumentsManifestName();
        assertEquals(META_INF + "datamanifest-1.tlv", documentsManifestName);
        verify(indexProvider, atLeastOnce()).getNextDocumentsManifestIndex();
    }

    @Test
    public void testNextManifestName() throws Exception {
        String manifestName = nameProvider.nextManifestName();
        assertEquals(META_INF + "manifest-1.tlv", manifestName);
        verify(indexProvider, atLeastOnce()).getNextManifestIndex();
    }

    @Test
    public void testNextSignatureName() throws Exception {
        String name = nameProvider.nextSignatureName();
        assertEquals(META_INF + "signature-1.ksi", name);
        verify(indexProvider, atLeastOnce()).getNextSignatureIndex();
    }

}