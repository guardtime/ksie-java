package com.guardtime.container.indexing;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IncrementingIndexProviderFactoryTest extends AbstractContainerTest {

    private IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();

    @Test
    public void testCreateWithValidContainer() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                build();
        try (Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(STRING_CONTAINER_ANNOTATION))) {
            IndexProvider indexProvider = indexProviderFactory.create(container);
            Assert.assertEquals("2", indexProvider.getNextSignatureIndex());
        }
    }

    @Test
    public void testCreateWithInvalidContainer() throws Exception {
        expectedException.expect(IndexingException.class);
        expectedException.expectMessage("Not an integer based index");

        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(mockedManifestFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                disableInternalVerification().
                build();
        try (Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(STRING_CONTAINER_ANNOTATION))) {
            indexProviderFactory.create(container);
        }
    }

    @Test
    public void testValuesIncrement() throws Exception {
        IndexProvider indexProvider = indexProviderFactory.create();
        int firstIndex = Integer.parseInt(indexProvider.getNextAnnotationIndex());
        int secondIndex = Integer.parseInt(indexProvider.getNextAnnotationIndex());
        assertTrue(firstIndex < secondIndex);
    }

    @Test
    public void testDifferentManifestIndexesStartFromSameValue() {
        IndexProvider indexProvider = indexProviderFactory.create();
        int manifestIndex = Integer.parseInt(indexProvider.getNextManifestIndex());
        int documentManifestIndex = Integer.parseInt(indexProvider.getNextDocumentsManifestIndex());
        assertEquals(manifestIndex, documentManifestIndex);
    }

}