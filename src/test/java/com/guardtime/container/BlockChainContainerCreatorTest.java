package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.packaging.BlockChainContainer;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BlockChainContainerCreatorTest extends AbstractBlockChainContainerTest {

    @Mock
    private BlockChainContainer blockChainContainer;

    @Test
    public void testCreateBuilder() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);
        assertNotNull(builder);
    }

    @Test
    public void testCreateBuilderWithoutPackagingFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new BlockChainContainerBuilder(null);
    }

    @Test
    public void testAddDocumentToContainer() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);
        StreamContainerDocument content = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        builder.withDataFile(content);
        assertEquals(1, builder.getDocuments().size());
    }

    @Test
    public void testAddAnnotationToContainer() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);
        StringAnnotation annotation = new StringAnnotation(ContainerAnnotationType.NON_REMOVABLE, "42", "com.guardtime");
        builder.withAnnotation(annotation);
        assertEquals(1, builder.getAnnotations().size());
    }

    @Test
    public void testBuildContainer() throws Exception {
        when(mockedPackagingFactory.create(Mockito.any(BlockChainContainer.class), Mockito.anyListOf(ContainerDocument.class), Mockito.anyListOf(ContainerAnnotation.class))).thenReturn(blockChainContainer);
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);

        BlockChainContainer container = builder.
                withDataFile(new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)).
                withAnnotation(new StringAnnotation(ContainerAnnotationType.NON_REMOVABLE, "42", "com.guardtime")).
                build();

        assertNotNull(container);
        verify(mockedPackagingFactory, ONE_EXECUTION).create(Mockito.any(BlockChainContainer.class), Mockito.anyList(), Mockito.anyList());
    }

}