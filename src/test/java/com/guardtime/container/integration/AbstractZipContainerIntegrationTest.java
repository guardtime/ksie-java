package com.guardtime.container.integration;

import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.parsing.ParsingStoreFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractZipContainerIntegrationTest extends AbstractCommonIntegrationTest {
    private ContainerPackagingFactory packagingFactoryWithIncIndex;
    private ContainerPackagingFactory packagingFactoryWithUuid;
    private ContainerPackagingFactory defaultPackagingFactory;
    private ParsingStoreFactory parsingStoreFactory;


    protected abstract ParsingStoreFactory getParsingStoreFactory();

    protected ContainerPackagingFactory getDefaultPackagingFactory() {
        return defaultPackagingFactory;
    }

    @Before
    public void setUpPackagingFactories() throws Exception {
        parsingStoreFactory = getParsingStoreFactory();
        this.packagingFactoryWithIncIndex = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                withParsingStoreFactory(parsingStoreFactory).
                build();
        this.packagingFactoryWithUuid = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                withParsingStoreFactory(parsingStoreFactory).
                build();
        this.defaultPackagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withParsingStoreFactory(parsingStoreFactory).
                build();
    }

    @Test
    public void testReadContainerWithMissingManifest() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Parsed container was not valid");
        try (Container ignored = getContainer(CONTAINER_WITH_MISSING_MANIFEST)) {}
    }

    @Test
    public void testReadContainerWithMissingMimetype() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Parsed container was not valid");
        try (Container ignored = getContainer(CONTAINER_WITH_MISSING_MIMETYPE)) {
        }
    }

    @Test
    public void testVerifyContainerWithEmptyMimetype() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Parsed container was not valid");
        try (Container ignored = getContainer(CONTAINER_WITH_MIMETYPE_IS_EMPTY)) {}
    }

    @Test
    public void testCreateContainer() throws Exception {
        try (
                Container container = new ContainerBuilder(defaultPackagingFactory)
                        .withDocument(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME_TEST_TXT, "application/txt")
                        .build()
        ) {
            assertSingleContentsWithSingleDocumentWithName(container, TEST_FILE_NAME_TEST_TXT);
        }
    }

    @Test
    public void testReadContainer() throws Exception {
        try (
                InputStream inputStream = new FileInputStream(loadFile(CONTAINER_WITH_ONE_DOCUMENT));
                Container container = defaultPackagingFactory.read(inputStream)
        ) {
            assertSingleContentsWithSingleDocument(container);
        }
    }

    @Test
    public void testReadCreatedContainer() throws Exception {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Container container = new ContainerBuilder(defaultPackagingFactory)
                        .withDocument(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME_TEST_TXT, "application/txt")
                        .build()
            ) {
                container.writeTo(bos);

                try (
                        InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
                        Container parsedInContainer = defaultPackagingFactory.read(inputStream)
                ) {
                    assertSingleContentsWithSingleDocument(parsedInContainer);
            }
        }
    }

    @Test
    public void testReadContainerWithDifferentIndexProviderCombination1_OK() throws Exception {
        try (Container container = packagingFactoryWithUuid.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION))) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadContainerWithDifferentIndexProviderCombination2_OK() throws Exception {
        try (Container container = packagingFactoryWithIncIndex.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION))) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
        }
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexProviderCombination_OK() throws Exception {
        try (
                Container existingContainer = packagingFactoryWithIncIndex.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
                Container container = packagingFactoryWithUuid.create(existingContainer, Collections.singletonList(TEST_DOCUMENT_HELLO_PDF), Collections.singletonList(STRING_CONTAINER_ANNOTATION))
        ){
            writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
        }
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexProviderCombination2_OK() throws Exception {
        try (
                Container existingContainer = packagingFactoryWithUuid.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
                Container container = packagingFactoryWithIncIndex.create(existingContainer, Collections.singletonList(TEST_DOCUMENT_HELLO_PDF), Collections.singletonList(STRING_CONTAINER_ANNOTATION))
            ){
                writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadContainerWithRandomIncrementingIndexesAndAddNewContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(CONTAINER_WITH_RANDOM_INCREMENTING_INDEXES));
                Container existingContainer = defaultPackagingFactory.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                ContainerDocument document = new StreamContainerDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                Container container = defaultPackagingFactory.create(existingContainer,
                        Collections.singletonList(document),
                        Collections.singletonList(STRING_CONTAINER_ANNOTATION))
        ) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadContainerWithRandomUuidIndexesAndAddNewContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(CONTAINER_WITH_RANDOM_UUID_INDEXES));
                Container existingContainer = packagingFactoryWithUuid.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                ContainerDocument document = new StreamContainerDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                Container container = packagingFactoryWithUuid.create(existingContainer,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_CONTAINER_ANNOTATION))
        ) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
        }
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexTypesInSameContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(CONTAINER_WITH_MIXED_INDEX_TYPES));
                Container existingContainer  = packagingFactoryWithUuid.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                ContainerDocument document = new StreamContainerDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                Container container = packagingFactoryWithUuid.create(existingContainer,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_CONTAINER_ANNOTATION))
        ) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
        }
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexesInContents_OK() throws Exception {
        try (
                Container existingContainer = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)));
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                Container container = packagingFactoryWithUuid.create(existingContainer,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_CONTAINER_ANNOTATION))
        ) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    @Ignore //Should be possible.
    @Test
    public void testAddDocumentsToExistingContainerWithOneContentRemoved_OK() throws Exception {
        try (
                Container existingContainer = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(CONTAINER_WITH_TWO_CONTENTS_AND_ONE_MANIFEST_REMOVED)));
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                ContainerAnnotation containerAnnotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "annotation 101", "com.guardtime");
                Container container = packagingFactoryWithIncIndex.create(existingContainer,
                    Collections.singletonList(document),
                    Collections.singletonList(containerAnnotation))
        ) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testAddDocumentsToExistingContainerUnknownFiles_OK() throws Exception {
        try (
                Container existingContainer = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(CONTAINER_WITH_UNKNOWN_FILES)));
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                ContainerAnnotation containerAnnotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "annotation 101", "com.guardtime");
                Container container = packagingFactoryWithIncIndex.create(existingContainer,
                        Collections.singletonList(document),
                        Collections.singletonList(containerAnnotation))
        ) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    @Ignore //Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesManifestUri_throws() throws Exception {
        String item = "META-INF/manifest-1.tlv";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesDocumentManifestUri_throws() throws Exception {
        String item = "META-INF/datamanifest-1.tlv";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesAnnotationsManifestUri_throws() throws Exception {
        String item = "META-INF/annotmanifest-1.tlv";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesSingleAnnotationManifestUri_throws() throws Exception {
        String item = "META-INF/annotation-1.tlv";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesAnnotationUri_throws() throws Exception {
        String item = "META-INF/annotation-1.dat";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because it should not be possible to add document with file ur is "mimetype". Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesMimeTypeUri_throws() throws Exception {
        String item = "mimetype";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesSignatureUri_throws() throws Exception {
        String item = "META-INF/signature-1.ksi";
        createContainerWriteItToAndReadFromStream(item);
    }

    @Ignore //Exception is expected because document file name is dir and it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentIsWrittenAsMetaInfDirectory_throws() throws Exception {
        createContainerWriteItToAndReadFromStream("META-INF/");
    }

    @Ignore //Exception is expected because document file name matches with META-INF directory name. Not because of duplicate entry.
    @Test
    public void testCreateContainerWhereDocumentFileUriMatchesMetaInfDirectoryName_throws() throws Exception {
        createContainerWriteItToAndReadFromStream("META-INF");
    }

    @Ignore //Exception is expected because document file name is dir
    @Test
    public void testCreateContainerWhereDocumentIsWrittenAsDirectory_throws() throws Exception {
        createContainerWriteItToAndReadFromStream("SubDir/");
    }

    @Test
    public void testCreateContainerWhereDocumentIsWrittenToSubDirectory_Ok() throws Exception {
        createContainerWriteItToAndReadFromStream("SubDir/AddedDocument.txt");
    }

    private List<ContainerDocument> getContainerDocument(String fileName) throws Exception {
        return Collections.singletonList((ContainerDocument)new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, fileName));
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

    /*
    Created container will be closed in the end.
     */
    private void createContainerWriteItToAndReadFromStream(String documentFileName) throws Exception {
        try (Container container = defaultPackagingFactory.create(getContainerDocument(documentFileName), Collections.singletonList(STRING_CONTAINER_ANNOTATION))) {
            writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
        }
    }

    /*
    Created container will be closed.
     */
    private void writeContainerToAndReadFromStream(Container container, ContainerPackagingFactory packagingFactory) throws Exception {
        assertNotNull(container);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            container.writeTo(bos);
            try (
                    ByteArrayInputStream stream = new ByteArrayInputStream(bos.toByteArray());
                    Container inputContainer = packagingFactory.read(stream)) {
                assertNotNull(inputContainer);
            }
        }
    }
}
