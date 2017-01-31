package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerMergingException;
import com.guardtime.container.packaging.ContainerPackagingFactory;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ZipContainerTest extends AbstractContainerTest {

    @Test
    public void testAddSingleSignatureContent_OK() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<>())) {
                container.add(newContainer.getSignatureContents().get(0));
                assertEquals(2, container.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddContainer_OK() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<>())) {
                container.add(newContainer);
                assertEquals(2, container.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddListOfSignatureContent_OK() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<>())) {
                StreamContainerDocument containerDocument =
                        new StreamContainerDocument(new ByteArrayInputStream("auh".getBytes(StandardCharsets.UTF_8)), "text/plain", "someTestFile.txt");
                packagingFactory.create(newContainer, singletonList(containerDocument), new ArrayList<>());
                container.add(newContainer);
                assertEquals(newContainer.getSignatureContents().size() + 1, container.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddWithSameManifestName_ThrowsContainerMergingException() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for Manifest!");
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<>())) {
                container.add(newContainer.getSignatureContents().get(0));
            }
        }
    }

    @Test
    public void testAddWithSameContainerDocument_ThrowsContainerMergingException() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for ContainerDocument!");
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), new ArrayList<>())) {
                container.add(newContainer.getSignatureContents().get(0));
            }
        }
    }
}
