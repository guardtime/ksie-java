package com.guardtime.container.integration;

import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ContainerMergingIntegrationTest extends AbstractCommonIntegrationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
    }

    @Test
    public void testMergeParsedContainerWithCreatedContainer() throws Exception {
        try (Container parsedContainer = getContainer(CONTAINER_WITH_RANDOM_UUID_INDEXES);
             Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new LinkedList<>())) {
            int expectedSignatureContentsSize =
                    parsedContainer.getSignatureContents().size() + newContainer.getSignatureContents().size();
            parsedContainer.add(newContainer);
            assertSignatureContentsCount(parsedContainer, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeParsedContainerWithCreatedSignatureContent() throws Exception {
        try (Container parsedContainer = getContainer(CONTAINER_WITH_RANDOM_UUID_INDEXES)) {
            int expectedSignatureContentsSize =
                    parsedContainer.getSignatureContents().size() + 1;
            parsedContainer.add(createSignatureContent());
            assertSignatureContentsCount(parsedContainer, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeParsedContainerWithCreatedSignatureContentList() throws Exception {
        try (Container parsedContainer = getContainer(CONTAINER_WITH_RANDOM_UUID_INDEXES)) {
            List<SignatureContent> signatureContents = new LinkedList<>();
            signatureContents.add(createSignatureContent());
            signatureContents.add(createSignatureContent());
            signatureContents.add(createSignatureContent());
            int expectedSignatureContentsSize =
                    parsedContainer.getSignatureContents().size() + signatureContents.size();
            parsedContainer.addAll(signatureContents);
            assertSignatureContentsCount(parsedContainer, expectedSignatureContentsSize);
        }
    }

    private SignatureContent createSignatureContent() throws Exception {
        StreamContainerDocument containerDocument = new StreamContainerDocument(
                new ByteArrayInputStream(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)),
                "text/plain",
                UUID.randomUUID().toString()
        );
        try (Container temp = packagingFactory.create(singletonList(containerDocument), new LinkedList<>())) {
            return temp.getSignatureContents().get(0);
        }
    }

    private void assertSignatureContentsCount(Container parsedContainer, int expectedSignatureContentsSize) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        parsedContainer.writeTo(bos);
        try (Container parsedMergedContainer = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
            assertEquals(expectedSignatureContentsSize, parsedMergedContainer.getSignatureContents().size());
        }
    }
}
