package com.guardtime.container.indexing;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class IncrementingIndexProviderFactoryTest extends AbstractContainerTest {

    private IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();

    @Test
    public void testCreateWithValidContainer() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory(), new IncrementingIndexProviderFactory(), true);
        Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(MOCKED_ANNOTATION));

        IndexProvider indexProvider = indexProviderFactory.create(container);
        Assert.assertEquals("2", indexProvider.getNextSignatureIndex());
    }

    @Test
    public void testCreateWithInvalidContainer() throws Exception {
        expectedException.expect(IndexingException.class);
        expectedException.expectMessage("Not an integer based index");

        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, new UuidIndexProviderFactory(), true);
        Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(MOCKED_ANNOTATION));
        indexProviderFactory.create(container);
    }
}