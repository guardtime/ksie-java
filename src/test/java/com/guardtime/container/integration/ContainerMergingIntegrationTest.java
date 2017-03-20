package com.guardtime.container.integration;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContainerMergingIntegrationTest extends AbstractCommonIntegrationTest {
    private ContainerPackagingFactory incPackagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        incPackagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
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

    @Test
    public void testMergeContainersWithDifferentIndexProviders1() throws Exception {
        try (   Container container1 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES);
                Container container2 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            container1.add(container2);
            container1.getSignatureContents();
            Assert.assertEquals(4, container1.getSignatureContents().size());
        }
    }

    @Test
    public void testMergeContainersWithDifferentIndexProviders2() throws Exception {
        try (   Container container1 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES);
                Container container2 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            container2.add(container1);
            Assert.assertEquals(4, container2.getSignatureContents().size());
        }
    }

    @Test
    public void testAddNewContentToMergedContainer1() throws Exception {
        try (ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream("".getBytes()), "textDoc", "1-" + Long.toString(new Date().getTime()));
             Container uuidContainer = packagingFactory.create(singletonList(document), singletonList(STRING_CONTAINER_ANNOTATION));
             Container incContainer = getContainer(CONTAINER_WITH_RANDOM_INCREMENTING_INDEXES);
             ContainerDocument document2 = new StreamContainerDocument(new ByteArrayInputStream("".getBytes()), "textDoc", "2-" + Long.toString(new Date().getTime()))) {
            uuidContainer.add(incContainer);
            try (Container newContainer = packagingFactory.create(uuidContainer, singletonList(document2), singletonList(STRING_CONTAINER_ANNOTATION))) {
                assertEquals(newContainer.getSignatureContents().size(), 4);
            }
        }
    }

    @Test
    public void testWritingMergedContainer_OK() throws Exception {
        try (Container parsedContainer = getContainer(CONTAINER_WITH_RANDOM_UUID_INDEXES);
             Container secondParsedContainer = getContainer(CONTAINER_WITH_RANDOM_INCREMENTING_INDEXES);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            int expectedSignatureContentCount =
                    parsedContainer.getSignatureContents().size() + secondParsedContainer.getSignatureContents().size();
            parsedContainer.add(secondParsedContainer);
            parsedContainer.writeTo(outputStream);
            assertNotNull(outputStream.toByteArray());
            assertTrue(outputStream.toByteArray().length > 0);
            try (Container mergedContainer = packagingFactory.read(new ByteArrayInputStream(outputStream.toByteArray()))) {
                assertEquals(expectedSignatureContentCount, mergedContainer.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddNewContentToMergedContainer2() throws Exception {
        try (   Container uuidContainer = getContainer(CONTAINER_WITH_RANDOM_UUID_INDEXES);
                Container incContainer = incPackagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), singletonList(STRING_CONTAINER_ANNOTATION));
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream("".getBytes()), "textDoc", Long.toString(new Date().getTime()))) {
            incContainer.add(uuidContainer);
            try (Container newContainer = incPackagingFactory.create(incContainer, singletonList(document), singletonList(STRING_CONTAINER_ANNOTATION))) {
                assertEquals(newContainer.getSignatureContents().size(), 3);
            }
        }
    }

    @Test
    public void testMergeContainersUnknownFileConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for ContainerDocument! Path: META-INF/sun.txt");
        mergeContainersWithConflicts(CONTAINERS_FOR_UNKNOWN_FILE_CONFLICT);
    }

    @Test
    public void testMergeContainersDocumentManifestConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing DocumentsManifest! Path: META-INF/datamanifest-1.tlv");
        mergeContainersWithConflicts(CONTAINERS_FOR_DOCUMENTS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersAnnotationDataConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-1.dat");
        mergeContainersWithConflicts(CONTAINERS_FOR_ANNOTATION_DATA_CONFLICT);
    }

    @Test
    public void testMergeContainersSingleAnnotationManifestConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing SingleAnnotationManifest! Path: META-INF/annotation-1.tlv");
        mergeContainersWithConflicts(CONTAINERS_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersAnnotationsManifestConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing AnnotationsManifest! Path: META-INF/annotmanifest-1.tlv");
        mergeContainersWithConflicts(CONTAINERS_FOR_ANNOTATIONS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersSignatureConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing signature! Path: META-INF/signature-1.ksi");
        mergeContainersWithConflicts(CONTAINERS_FOR_SIGNATURE_CONFLICT);
    }

    @Test
    public void testMergeContainersDocumentConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for ContainerDocument!");
        mergeContainersWithConflicts(CONTAINERS_FOR_DOCUMENT_CONFLICT);
    }

    @Test
    public void testMergeContainersManifestConflict() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Manifest! Path: META-INF/manifest-1.tlv");
        mergeContainersWithConflicts(CONTAINERS_FOR_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersUnknownConflictsWithContainerFile1() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeContainersWithConflicts(CONTAINERS_FOR_MIX_CONFLICT_1);
    }

    @Test
    public void testMergeContainersUnknownConflictsWithContainerFile2() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeContainersWithConflicts(CONTAINERS_FOR_MIX_CONFLICT_2);
    }

    @Ignore //TODO-77
    @Test
    public void testMergeContainersWithExactSameDocument() throws Exception {
        try (Container container = mergeContainers(CONTAINERS_FOR_SAME_DOCUMENT)) {
            assertEquals(2, container.getSignatureContents().size());
        }
    }

    @Ignore //TODO-77
    @Test
    public void testMergeContainerWithExactSameContainer() throws Exception {
        try (Container container = mergeContainers(CONTAINERS_IDENTICAL)) {
            assertEquals(2, container.getSignatureContents().size());
            assertSignatureContentsCount(container, 1);
        }
    }

    private void mergeContainersWithConflicts(String[] containers) throws Exception {
        try (   Container container1 = getContainer(containers[0]);
                Container container2 = getContainer(containers[1])) {
            container1.add(container2);
        }
    }

    private Container mergeContainers(String[] containers) throws Exception {
        Container container1 = getContainer(containers[0]);
        try (Container container2 = getContainer(containers[1])) {
            container1.add(container2);
        }
        return container1;
    }


    private SignatureContent createSignatureContent(ContainerDocument existingDocument) throws Exception {
        ContainerDocument containerDocument = existingDocument;
        if (containerDocument == null) {
            containerDocument = new StreamContainerDocument(
                    new ByteArrayInputStream(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)),
                    "text/plain",
                    UUID.randomUUID().toString()
            );
        }
        try (Container temp = packagingFactory.create(singletonList(containerDocument), new LinkedList<>())) {
            return temp.getSignatureContents().get(0);
        }
    }

    private SignatureContent createSignatureContent() throws Exception {
        return createSignatureContent(null);
    }

    private void assertSignatureContentsCount(Container parsedContainer, int expectedSignatureContentsSize) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        parsedContainer.writeTo(bos);
        try (Container containerFromBos = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
            assertEquals(expectedSignatureContentsSize, containerFromBos.getSignatureContents().size());
        }
    }
}
