package com.guardtime.container.integration;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.extending.ExtendedContainer;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.container.packaging.exception.ContainerAnnotationMergingException;
import com.guardtime.container.packaging.exception.DocumentMergingException;
import com.guardtime.container.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.container.packaging.exception.ManifestMergingException;
import com.guardtime.container.packaging.exception.SignatureMergingException;
import com.guardtime.container.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.verification.VerifiedContainer;
import com.guardtime.container.verification.result.ResultHolder;

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


    /**
     * Containers - for creating file conflicts when trying to merge containers.
     */
    private static final String[] CONTAINERS_FOR_UNKNOWN_FILE_CONFLICT = {"containers/container-for-unknown-file-conflict.ksie", CONTAINER_WITH_UNKNOWN_FILES};
    private static final String[] CONTAINERS_FOR_DOCUMENTS_MANIFEST_CONFLICT = {"containers/container-for-documents-manifest-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_ANNOTATION_DATA_CONFLICT = {"containers/container-for-annotation-data-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT = {"containers/container-for-annotation-manifest-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_ANNOTATIONS_MANIFEST_CONFLICT = {"containers/container-for-annotations-manifest-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_DOCUMENT_CONFLICT = {"containers/container-for-document-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_MANIFEST_CONFLICT = {"containers/container-for-manifest-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_SIGNATURE_CONFLICT = {"containers/container-for-signature-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_MIX_CONFLICT_1 = {"containers/container-for-mix-conflict.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_MIX_CONFLICT_2 = {CONTAINER_WITH_MULTIPLE_ANNOTATIONS, "containers/container-for-mix-conflict.ksie"};

    /**
     * Containers - merging those container should not yield any exception.
     */
    private static final String[] CONTAINERS_FOR_SAME_DOCUMENT = {"containers/container-for-same-document-file.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_IDENTICAL = {"containers/container-multiple-annotations-copy.ksie", CONTAINER_WITH_MULTIPLE_ANNOTATIONS};

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
        try (Container container1 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES);
             Container container2 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            container1.add(container2);
            Assert.assertEquals(4, container1.getSignatureContents().size());
        }
    }

    @Test
    public void testMergeContainersWithDifferentIndexProviders2() throws Exception {
        try (Container container1 = getContainer(CONTAINER_WITH_MIXED_INDEX_TYPES);
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
        try (Container uuidContainer = getContainer(CONTAINER_WITH_RANDOM_UUID_INDEXES);
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
        expectedException.expect(DocumentMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for ContainerDocument! Path: META-INF/sun.txt");
        mergeContainers(CONTAINERS_FOR_UNKNOWN_FILE_CONFLICT);
    }

    @Test
    public void testMergeContainersDocumentManifestConflict() throws Exception {
        expectedException.expect(DocumentsManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing DocumentsManifest! Path: META-INF/datamanifest-1.tlv");
        mergeContainers(CONTAINERS_FOR_DOCUMENTS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersAnnotationDataConflict() throws Exception {
        expectedException.expect(ContainerAnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-1.dat");
        mergeContainers(CONTAINERS_FOR_ANNOTATION_DATA_CONFLICT);
    }

    @Test
    public void testMergeContainersSingleAnnotationManifestConflict() throws Exception {
        expectedException.expect(SingleAnnotationManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing SingleAnnotationManifest! Path: META-INF/annotation-1.tlv");
        mergeContainers(CONTAINERS_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersAnnotationsManifestConflict() throws Exception {
        expectedException.expect(AnnotationsManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing AnnotationsManifest! Path: META-INF/annotmanifest-1.tlv");
        mergeContainers(CONTAINERS_FOR_ANNOTATIONS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersSignatureConflict() throws Exception {
        expectedException.expect(SignatureMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing signature! Path: META-INF/signature-1.ksi");
        mergeContainers(CONTAINERS_FOR_SIGNATURE_CONFLICT);
    }

    @Test
    public void testMergeContainersDocumentConflict() throws Exception {
        expectedException.expect(DocumentMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for ContainerDocument!");
        mergeContainers(CONTAINERS_FOR_DOCUMENT_CONFLICT);
    }

    @Test
    public void testMergeContainersManifestConflict() throws Exception {
        expectedException.expect(ManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Manifest! Path: META-INF/manifest-1.tlv");
        mergeContainers(CONTAINERS_FOR_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeContainersUnknownConflictsWithContainerFile1() throws Exception {
        expectedException.expect(ContainerAnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeContainers(CONTAINERS_FOR_MIX_CONFLICT_1);
    }

    @Test
    public void testMergeContainersUnknownConflictsWithContainerFile2() throws Exception {
        expectedException.expect(ContainerAnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeContainers(CONTAINERS_FOR_MIX_CONFLICT_2);
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

    @Test
    public void testAddContentToVerifiedContainer() throws Exception {
        try (VerifiedContainer verifiedContainer = new VerifiedContainer(getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addContent(verifiedContainer, 2);
        }
    }

    @Test
    public void testAddContainerToVerifiedContainer() throws Exception {
        try (VerifiedContainer verifiedContainer = new VerifiedContainer(getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addContainer(verifiedContainer, 2);
        }
    }

    @Test
    public void testAddAllContentsToVerifiedContainer() throws Exception {
        try (VerifiedContainer verifiedContainer = new VerifiedContainer(getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addAllContents(verifiedContainer, 4);
        }
    }

    @Test
    public void testAddContentToExtendedContainer() throws Exception {
        try (ExtendedContainer extendedContainer = new ExtendedContainer(getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT))) {
            addContent(extendedContainer, 2);
        }
    }

    @Test
    public void testAddContainerToExtendedContainer() throws Exception {
        try (ExtendedContainer extendedContainer = new ExtendedContainer(getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT))) {
            addContainer(extendedContainer, 2);
        }
    }

    @Test
    public void testAddAllContentsToExtendedContainer() throws Exception {
        try (ExtendedContainer extendedContainer = new ExtendedContainer(getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT))) {
            addAllContents(extendedContainer, 4);
        }
    }

    private void addContent(Container target, int expectedSize) throws Exception {
        try (Container source = getContainerIgnoreExceptions(CONTAINER_WITH_NO_DOCUMENTS)) {
            target.add(source.getSignatureContents().get(0));
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void addAllContents(Container target, int expectedSize) throws Exception {
        try (Container source = getContainerIgnoreExceptions(CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            target.addAll(source.getSignatureContents());
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void addContainer(Container target, int expectedSize) throws Exception {
        try (Container source = getContainerIgnoreExceptions(CONTAINER_WITH_NO_DOCUMENTS)) {
            target.add(source);
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private Container mergeContainers(String[] containers) throws Exception {
        try (Container container1 = getContainer(containers[0]);
             Container container2 = getContainer(containers[1])) {
            container1.add(container2);
            return container1;
        }
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
