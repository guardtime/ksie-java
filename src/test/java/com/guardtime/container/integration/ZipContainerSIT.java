package com.guardtime.container.integration;


import com.guardtime.container.AbstractCommonServiceIntegrationTest;
import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZipContainerSIT extends AbstractCommonServiceIntegrationTest {

    @Test
    public void testCreateContainer() throws Exception {
        Container container = new ContainerBuilder(packagingFactory)
                .withDataFile(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME_TEST_TXT, "application/txt")
                .build();
        assertSingleContentsWithSingleDocumentWithName(container, TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testReadContainer() throws Exception {
        InputStream stream = new FileInputStream(loadFile(CONTAINER_WITH_ONE_DOCUMENT));
        Container container = packagingFactory.read(stream);
        assertSingleContentsWithSingleDocument(container);
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
