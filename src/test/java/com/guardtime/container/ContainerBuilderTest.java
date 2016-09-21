package com.guardtime.container;

import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProvider;
import com.guardtime.container.indexing.IndexProvider;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


public class ContainerBuilderTest extends AbstractContainerTest {

    @Mock
    private ContainerPackagingFactory mockedPackagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedPackagingFactory.create(Mockito.anyList(), Mockito.anyList())).thenReturn(Mockito.mock(Container.class));
    }

    @Test
    public void testCreateBuilder() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        assertNotNull(builder);
    }

    @Test
    public void testCreateBuilderWithoutPackagingFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new ContainerBuilder(null);
    }

    @Test
    public void testAddDocumentToContainer() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        StreamContainerDocument content = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        builder.withDocument(content);
        assertEquals(1, builder.getDocuments().size());

        builder.withDocument(Mockito.mock(File.class), "application/binary");
        assertEquals(2, builder.getDocuments().size());
    }

    @Test
    public void testAddAnnotationToContainer() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        builder.withAnnotation(MOCKED_ANNOTATION);
        assertEquals(1, builder.getAnnotations().size());
    }

    @Test
    public void testCreateSignature() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_TEXT);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

        builder.withAnnotation(MOCKED_ANNOTATION);
        Container container = builder.build();
        assertNotNull(container);
    }

    @Test
    public void testCreateWithExistingContainer() throws Exception {
        ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, mockedManifestFactory, Mockito.mock(IndexProvider.class));
        // build initial container
        ContainerBuilder builder = new ContainerBuilder(packagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);
        Container container = builder.build();

        // add new documents to existing container
        ContainerBuilder newBuilder = new ContainerBuilder(packagingFactory);
        newBuilder.withDocument(TEST_DOCUMENT_HELLO_TEXT);
        newBuilder.withExistingContainer(container);
        Container newContainer = newBuilder.build();

        assertNotNull(newContainer);
        assertEquals(2, newContainer.getSignatureContents().size());
    }

}