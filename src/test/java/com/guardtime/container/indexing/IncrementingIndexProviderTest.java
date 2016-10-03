package com.guardtime.container.indexing;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class IncrementingIndexProviderTest extends AbstractContainerTest {

    private IndexProvider indexProvider = new IncrementingIndexProvider();

    @Test
    public void testUpdateWithValidContainer() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory(), new IncrementingIndexProvider(), true);
        Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(MOCKED_ANNOTATION));

        indexProvider.updateIndexes(container);
        Assert.assertEquals("2", indexProvider.getNextSignatureIndex());
    }

    @Test
    public void testUpdateWithInvalidContainer() throws Exception {
        expectedException.expect(IndexingException.class);
        expectedException.expectMessage("Not an integer based index");

        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, Mockito.mock(IndexProvider.class), true);
        Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(MOCKED_ANNOTATION));
        indexProvider.updateIndexes(container);
    }
}