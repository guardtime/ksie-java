package com.guardtime.container;

import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;
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
    public void testCreateBuilderWithoutPackagingFactory_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new ContainerBuilder(null);
    }

    @Test
    public void testAddDocumentToContainer() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        StreamContainerDocument content = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        builder.withDataFile(content);
        assertEquals(1, builder.getDocuments().size());
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
        builder.withDataFile(TEST_DOCUMENT_HELLO_TEXT);
        builder.withDataFile(TEST_DOCUMENT_HELLO_PDF);

        builder.withAnnotation(MOCKED_ANNOTATION);
        Container container = builder.build();
        assertNotNull(container);
    }

}