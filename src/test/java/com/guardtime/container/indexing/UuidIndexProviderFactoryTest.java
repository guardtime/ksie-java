package com.guardtime.container.indexing;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import org.junit.Test;
import java.util.Arrays;

import static org.junit.Assert.assertNotEquals;

public class UuidIndexProviderFactoryTest extends AbstractContainerTest {

    private IndexProviderFactory indexProviderFactory = new UuidIndexProviderFactory();

    @Test
    public void testCreateWithInvalidContainer() throws Exception {
        expectedException.expect(IndexingException.class);
        expectedException.expectMessage("Not a RFC4122 UUID based index");

        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, new IncrementingIndexProviderFactory(), true);
        try (Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(STRING_CONTAINER_ANNOTATION))) {
            indexProviderFactory.create(container);
        }
    }

    @Test
    public void testNextValueDiffersFromPrevious() {
        IndexProvider indexProvider = indexProviderFactory.create();
        assertNotEquals(indexProvider.getNextAnnotationIndex(), indexProvider.getNextAnnotationIndex());
    }

    @Test
    public void testDifferentManifestIndexesDoNotMatch() {
        IndexProvider indexProvider = indexProviderFactory.create();
        assertNotEquals(indexProvider.getNextManifestIndex(), indexProvider.getNextDocumentsManifestIndex());
    }

}