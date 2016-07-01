package com.guardtime.container.integration;


import com.guardtime.container.AbstractCommonKsiServiceIntegrationTest;
import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZipContainerKsiServiceIT extends AbstractCommonKsiServiceIntegrationTest {

    @Test
    public void testCreateContainer() throws Exception {
        Container container = new ContainerBuilder(packagingFactory)
                .withDocument(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME_TEST_TXT, "application/txt")
                .build();
        assertSingleContentsWithSingleDocumentWithName(container, TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testReadContainer() throws Exception {
        InputStream stream = new FileInputStream(loadFile(CONTAINER_WITH_ONE_DOCUMENT));
        Container container = packagingFactory.read(stream);
        assertSingleContentsWithSingleDocument(container);
    }

    @Test
    public void testReadCreatedContainer() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Container container = new ContainerBuilder(packagingFactory)
                .withDocument(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME_TEST_TXT, "application/txt")
                .build();
        container.writeTo(bos);
        InputStream stream = new ByteArrayInputStream(bos.toByteArray());
        Container parsedInContainer = packagingFactory.read(stream);
        assertSingleContentsWithSingleDocument(parsedInContainer);
    }

    private void assertSingleContentsWithSingleDocumentWithName(Container container, String testFileName) {
        List<? extends SignatureContent> contents = container.getSignatureContents();
        assertNotNull(contents);
        assertEquals(1, contents.size());

        SignatureContent content = contents.get(0);
        assertNotNull(content);
        Map<String, ContainerDocument> documents = content.getDocuments();
        assertEquals(1, documents.size());
        if (testFileName != null) {
            assertNotNull(documents.get(testFileName));
        }
    }

    private void assertSingleContentsWithSingleDocument(Container container) {
        assertSingleContentsWithSingleDocumentWithName(container, null);
    }

}
