package com.guardtime.container.indexing;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotEquals;

public class UuidIndexProviderFactoryTest extends AbstractContainerTest {

    private IndexProviderFactory indexProviderFactory = new UuidIndexProviderFactory();

    @Test
    public void testCreateWithMixedContainer() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(mockedManifestFactory).
                disableInternalVerification().
                build();
        try (Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(STRING_CONTAINER_ANNOTATION))) {
            IndexProvider indexProvider = indexProviderFactory.create(container);
            Assert.assertNotNull(indexProvider);
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