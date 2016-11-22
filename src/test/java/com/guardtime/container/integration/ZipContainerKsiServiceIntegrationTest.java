package com.guardtime.container.integration;

import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZipContainerKsiServiceIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {

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

    @Test
    public void testReadContainerWithDifferentIndexProviderCombination1_OK() throws Exception {
        Container container = packagingFactoryWithUuid.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
    }

    @Test
    public void testReadContainerWithDifferentIndexProviderCombination2_OK() throws Exception {
        Container container = packagingFactoryWithIncIndex.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexProviderCombination_OK() throws Exception {
        Container existingContainer = packagingFactoryWithIncIndex.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        Container container = packagingFactoryWithUuid.create(existingContainer, Collections.singletonList(TEST_DOCUMENT_HELLO_PDF), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexProviderCombination2_OK() throws Exception {
        Container existingContainer = packagingFactoryWithUuid.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        Container container = packagingFactoryWithIncIndex.create(existingContainer, Collections.singletonList(TEST_DOCUMENT_HELLO_PDF), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
    }

    @Test
    public void testReadContainerWithRandomIncrementingIndexesAndAddNewContent_OK() throws Exception {
        Container existingContainer = packagingFactory.read(new FileInputStream(loadFile("containers/multi-content-random-incrementing-indexes.ksie")));
        Container container = packagingFactory.create(existingContainer,
                Collections.singletonList((ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc")),
                Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
    }

    @Test
    public void testReadContainerWithRandomUuidIndexesAndAddNewContent_OK() throws Exception {
        Container existingContainer = packagingFactoryWithUuid.read(new FileInputStream(loadFile("containers/container-random-uuid-indexes.ksie")));
        Container container = packagingFactoryWithUuid.create(existingContainer,
                Collections.singletonList((ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc")),
                Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexTypesInSameContent_OK() throws Exception {
        Container existingContainer  = packagingFactoryWithUuid.read(new FileInputStream(loadFile("containers/container-content-with-mixed-index-types.ksie")));
        Container container = packagingFactoryWithUuid.create(existingContainer,
                Collections.singletonList((ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc")),
                Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithUuid);
    }

    @Ignore //Should be possible.
    @Test
    public void testCreateContainerFromExistingWithDifferentIndexesInContents_OK() throws Exception {
        Container existingContainer = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile("containers/container-contents-with-different-index-types.ksie")));
        Container container = packagingFactoryWithUuid.create(existingContainer,
                Collections.singletonList((ContainerDocument) new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc")),
                Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
    }

    @Ignore //Should be possible.
    @Test
    public void testAddDocumentsToExistingContainerWithUnknownFiles_OK() throws Exception {
        Container container = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile("containers/container-two-contents-one-manifest-removed.ksie")));
        Container container2 = packagingFactoryWithIncIndex.create(container,
                Collections.singletonList((ContainerDocument)new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc")),
                Collections.singletonList((ContainerAnnotation) new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "annotation 101", "com.guardtime")));
        writeContainerToAndReadFromStream(container2, packagingFactoryWithIncIndex);
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

    private void createContainerWriteItToAndReadFromStream(String documentFileName) throws Exception {
        Container container = packagingFactory.create(getContainerDocument(documentFileName), Collections.singletonList(STRING_CONTAINER_ANNOTATION));
        writeContainerToAndReadFromStream(container, packagingFactoryWithIncIndex);
    }

    private void writeContainerToAndReadFromStream(Container container, ZipContainerPackagingFactory packagingFactory) throws Exception {
        assertNotNull(container);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        container.writeTo(bos);

        Container inputContainer = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()));
        assertNotNull(inputContainer);
    }
}
