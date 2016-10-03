package com.guardtime.container.indexing;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class UuidIndexProviderTest extends AbstractContainerTest {

    private IndexProvider indexProvider = new UuidIndexProvider();

    @Test
    public void testUpdateWithInvalidContainer() throws Exception {
        expectedException.expect(IndexingException.class);
        expectedException.expectMessage("Not a RFC4122 UUID based index");

        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, Mockito.mock(IndexProvider.class), true);
        Container container = packagingFactory.create(Arrays.asList(TEST_DOCUMENT_HELLO_TEXT), Arrays.asList(MOCKED_ANNOTATION));
        indexProvider.updateIndexes(container);
    }

}