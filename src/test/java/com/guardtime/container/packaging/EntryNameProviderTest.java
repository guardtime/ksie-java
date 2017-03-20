package com.guardtime.container.packaging;

import com.guardtime.container.indexing.IndexProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntryNameProviderTest {

    private static final String INDEX_STRING = "101";
    private static final String MANIFEST_SUFFIX = "tlv";
    private static final String SIGNATURE_SUFFIX = "ksi";
    private IndexProvider indexProvider;
    private EntryNameProvider nameProvider;

    @Before
    public void setUp() {
        indexProvider = Mockito.mock(IndexProvider.class);
        nameProvider = new EntryNameProvider(MANIFEST_SUFFIX, SIGNATURE_SUFFIX, indexProvider);
    }

    @Test
    public void testNextSignatureName() throws Exception {
        when(indexProvider.getNextSignatureIndex()).thenReturn(INDEX_STRING);
        String name = nameProvider.nextSignatureName();
        assertEquals(format(EntryNameProvider.SIGNATURE_FORMAT, INDEX_STRING, SIGNATURE_SUFFIX), name);
        verify(indexProvider, atLeastOnce()).getNextSignatureIndex();
    }

    @Test
    public void testNextManifestName() throws Exception {
        when(indexProvider.getNextManifestIndex()).thenReturn(INDEX_STRING);
        String manifestName = nameProvider.nextManifestName();
        assertEquals(format(EntryNameProvider.MANIFEST_FORMAT, INDEX_STRING, MANIFEST_SUFFIX), manifestName);
        verify(indexProvider, atLeastOnce()).getNextManifestIndex();
    }

    @Test
    public void testNextDocumentsManifestName() throws Exception {
        when(indexProvider.getNextDocumentsManifestIndex()).thenReturn(INDEX_STRING);
        String documentsManifestName = nameProvider.nextDocumentsManifestName();
        assertEquals(format(EntryNameProvider.DOCUMENTS_MANIFEST_FORMAT, INDEX_STRING, MANIFEST_SUFFIX), documentsManifestName);
        verify(indexProvider, atLeastOnce()).getNextDocumentsManifestIndex();
    }

    @Test
    public void testNextAnnotationsManifestName() throws Exception {
        when(indexProvider.getNextAnnotationsManifestIndex()).thenReturn(INDEX_STRING);
        String manifestName = nameProvider.nextAnnotationsManifestName();
        assertEquals(format(EntryNameProvider.ANNOTATIONS_MANIFEST_FORMAT, INDEX_STRING, MANIFEST_SUFFIX), manifestName);
        verify(indexProvider, atLeastOnce()).getNextAnnotationsManifestIndex();
    }

    @Test
    public void testNextSingleAnnotationManifestName() throws Exception {
        when(indexProvider.getNextSingleAnnotationManifestIndex()).thenReturn(INDEX_STRING);
        String manifestName = nameProvider.nextSingleAnnotationManifestName();
        assertEquals(format(EntryNameProvider.SINGLE_ANNOTATION_MANIFEST_FORMAT, INDEX_STRING, MANIFEST_SUFFIX), manifestName);
        verify(indexProvider, atLeastOnce()).getNextSingleAnnotationManifestIndex();
    }

    @Test
    public void testNextAnnotationDataFileName() throws Exception {
        when(indexProvider.getNextAnnotationIndex()).thenReturn(INDEX_STRING);
        String name = nameProvider.nextAnnotationDataFileName();
        assertEquals(format(EntryNameProvider.ANNOTATION_DATA_FORMAT, INDEX_STRING, MANIFEST_SUFFIX), name);
        verify(indexProvider, atLeastOnce()).getNextAnnotationIndex();
    }

}