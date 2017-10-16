/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.integration;

import com.guardtime.envelope.EnvelopeBuilder;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.annotation.StringAnnotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.StreamDocument;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.InvalidPackageException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractZipEnvelopeIntegrationTest extends AbstractCommonIntegrationTest {
    private EnvelopePackagingFactory packagingFactoryWithIncIndex;
    private EnvelopePackagingFactory packagingFactoryWithUuid;
    private EnvelopePackagingFactory defaultPackagingFactory;
    private ParsingStoreFactory parsingStoreFactory;


    protected abstract ParsingStoreFactory getParsingStoreFactory();

    protected EnvelopePackagingFactory getDefaultPackagingFactory() {
        return defaultPackagingFactory;
    }

    @Before
    public void setUpPackagingFactories() throws Exception {
        parsingStoreFactory = getParsingStoreFactory();
        this.packagingFactoryWithIncIndex = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                withParsingStoreFactory(parsingStoreFactory).
                build();
        this.packagingFactoryWithUuid = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                withParsingStoreFactory(parsingStoreFactory).
                build();
        this.defaultPackagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withParsingStoreFactory(parsingStoreFactory).
                build();
    }

    @Test
    public void testReadEnvelopeWithMissingManifest() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Reading envelope encountered errors!");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MISSING_MANIFEST)) {}
    }

    @Test
    public void testReadEnvelopeWithMissingMimetype() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("No parsable MIME type.");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MISSING_MIMETYPE)) {
        }
    }

    @Test
    public void testVerifyEnvelopeWithEmptyMimetype() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Parsed Envelope has invalid MIME type. Can't process it!");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MIMETYPE_IS_EMPTY)) {}
    }

    @Test
    public void testCreateEnvelope() throws Exception {
        try (
                Envelope envelope = new EnvelopeBuilder(defaultPackagingFactory)
                        .withDocument(new ByteArrayInputStream("Test_Data".getBytes(StandardCharsets.UTF_8)), TEST_FILE_NAME_TEST_TXT, "application/txt")
                        .build()
        ) {
            assertSingleContentsWithSingleDocumentWithName(envelope, TEST_FILE_NAME_TEST_TXT);
        }
    }

    @Test
    public void testReadEnvelope() throws Exception {
        try (
                InputStream inputStream = new FileInputStream(loadFile(ENVELOPE_WITH_ONE_DOCUMENT));
                Envelope envelope = defaultPackagingFactory.read(inputStream)
        ) {
            assertSingleContentsWithSingleDocument(envelope);
        }
    }

    @Test
    public void testReadCreatedEnvelope() throws Exception {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Envelope envelope = new EnvelopeBuilder(defaultPackagingFactory)
                        .withDocument(new ByteArrayInputStream("Test_Data".getBytes(StandardCharsets.UTF_8)), TEST_FILE_NAME_TEST_TXT, "application/txt")
                        .build()
            ) {
                envelope.writeTo(bos);

                try (
                        InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
                        Envelope parsedInEnvelope = defaultPackagingFactory.read(inputStream)
                ) {
                    assertSingleContentsWithSingleDocument(parsedInEnvelope);
            }
        }
    }

    @Test
    public void testReadEnvelopeWithDifferentIndexProviderCombination1_OK() throws Exception {
        try (Envelope envelope = packagingFactoryWithUuid.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_ENVELOPE_ANNOTATION))) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadEnvelopeWithDifferentIndexProviderCombination2_OK() throws Exception {
        try (Envelope envelope = packagingFactoryWithIncIndex.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_ENVELOPE_ANNOTATION))) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexProviderCombination_OK() throws Exception {
        try (
                Envelope existingEnvelope = packagingFactoryWithIncIndex.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_ENVELOPE_ANNOTATION))
        ){
            packagingFactoryWithUuid.addSignature(existingEnvelope, Collections.singletonList(TEST_DOCUMENT_HELLO_PDF), Collections.singletonList(STRING_ENVELOPE_ANNOTATION));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexProviderCombination2_OK() throws Exception {
        try (
                Envelope existingEnvelope = packagingFactoryWithUuid.create(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), Collections.singletonList(STRING_ENVELOPE_ANNOTATION))
            ){
                packagingFactoryWithIncIndex.addSignature(existingEnvelope, Collections.singletonList(TEST_DOCUMENT_HELLO_PDF), Collections.singletonList(STRING_ENVELOPE_ANNOTATION));
                writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadEnvelopeWithRandomIncrementingIndexesAndAddNewContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_RANDOM_INCREMENTING_INDEXES));
                Envelope existingEnvelope = defaultPackagingFactory.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            defaultPackagingFactory.addSignature(existingEnvelope,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_ENVELOPE_ANNOTATION));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadEnvelopeWithRandomUuidIndexesAndAddNewContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_RANDOM_UUID_INDEXES));
                Envelope existingEnvelope = packagingFactoryWithUuid.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            packagingFactoryWithUuid.addSignature(existingEnvelope,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_ENVELOPE_ANNOTATION));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexTypesInSameContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES));
                Envelope existingEnvelope = packagingFactoryWithUuid.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            packagingFactoryWithUuid.addSignature(existingEnvelope,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_ENVELOPE_ANNOTATION));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexesInContents_OK() throws Exception {
        try (
                Envelope existingEnvelope = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)));
                Document document = new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            packagingFactoryWithUuid.addSignature(existingEnvelope,
                    Collections.singletonList(document),
                    Collections.singletonList(STRING_ENVELOPE_ANNOTATION));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testAddDocumentsToExistingEnvelopeWithOneContentRemoved_OK() throws Exception {
        try (
                Envelope existingEnvelope = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(ENVELOPE_WITH_TWO_CONTENTS_AND_ONE_MANIFEST_REMOVED)));
                Document document = new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                Annotation annotation = new StringAnnotation(EnvelopeAnnotationType.FULLY_REMOVABLE, "annotation 101", "com.guardtime")
        ) {
            packagingFactoryWithIncIndex.addSignature(existingEnvelope,
                    Collections.singletonList(document),
                    Collections.singletonList(annotation));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testAddDocumentsToExistingEnvelopeUnknownFiles_OK() throws Exception {
        try (
                Envelope existingEnvelope = packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(ENVELOPE_WITH_UNKNOWN_FILES)));
                Document document = new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, "Doc.doc");
                Annotation annotation = new StringAnnotation(EnvelopeAnnotationType.FULLY_REMOVABLE, "annotation 101", "com.guardtime")
        ) {
            packagingFactoryWithIncIndex.addSignature(existingEnvelope,
                    Collections.singletonList(document),
                    Collections.singletonList(annotation));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesManifestUri_throws() throws Exception {
        String item = "META-INF/manifest-1.tlv";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesDocumentManifestUri_throws() throws Exception {
        String item = "META-INF/datamanifest-1.tlv";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesAnnotationsManifestUri_throws() throws Exception {
        String item = "META-INF/annotmanifest-1.tlv";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesSingleAnnotationManifestUri_throws() throws Exception {
        String item = "META-INF/annotation-1.tlv";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesAnnotationUri_throws() throws Exception {
        String item = "META-INF/annotation-1.dat";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add document with file ur is "mimetype". Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesMimeTypeUri_throws() throws Exception {
        String item = "mimetype";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-53 Exception is expected because it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesSignatureUri_throws() throws Exception {
        String item = "META-INF/signature-1.ksi";
        createEnvelopeWriteItToAndReadFromStream(item);
    }

    @Ignore //TODO-54 Exception is expected because document file name is dir and it should not be possible to add documents to META-INF. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentIsWrittenAsMetaInfDirectory_throws() throws Exception {
        createEnvelopeWriteItToAndReadFromStream("META-INF/");
    }

    @Ignore //TODO-54 Exception is expected because document file name matches with META-INF directory name. Not because of duplicate entry.
    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesMetaInfDirectoryName_throws() throws Exception {
        createEnvelopeWriteItToAndReadFromStream("META-INF");
    }

    @Ignore //TODO-54 Exception is expected because document file name is dir
    @Test
    public void testCreateEnvelopeWhereDocumentIsWrittenAsDirectory_throws() throws Exception {
        createEnvelopeWriteItToAndReadFromStream("SubDir/");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentIsWrittenToSubDirectory_Ok() throws Exception {
        createEnvelopeWriteItToAndReadFromStream("SubDir/AddedDocument.txt");
    }

    private List<Document> getEnvelopeDocument(String fileName) throws Exception {
        return Collections.singletonList((Document)new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, fileName));
    }

    private void assertSingleContentsWithSingleDocumentWithName(Envelope envelope, String testFileName) {
        List<? extends SignatureContent> contents = envelope.getSignatureContents();
        assertNotNull(contents);
        assertEquals(1, contents.size());

        SignatureContent content = contents.get(0);
        assertNotNull(content);
        Map<String, Document> documents = content.getDocuments();
        assertEquals(1, documents.size());
        if (testFileName != null) {
            assertNotNull(documents.get(testFileName));
        }
    }

    private void assertSingleContentsWithSingleDocument(Envelope envelope) {
        assertSingleContentsWithSingleDocumentWithName(envelope, null);
    }

    /*
    Created envelope will be closed in the end.
     */
    private void createEnvelopeWriteItToAndReadFromStream(String documentFileName) throws Exception {
        try (Envelope envelope = defaultPackagingFactory.create(getEnvelopeDocument(documentFileName), Collections.singletonList(STRING_ENVELOPE_ANNOTATION))) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactoryWithIncIndex);
        }
    }

    /*
    Created envelope will be closed.
     */
    private void writeEnvelopeToAndReadFromStream(Envelope envelope, EnvelopePackagingFactory packagingFactory) throws Exception {
        assertNotNull(envelope);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            envelope.writeTo(bos);
            try (
                    ByteArrayInputStream stream = new ByteArrayInputStream(bos.toByteArray());
                    Envelope inputEnvelope = packagingFactory.read(stream)) {
                assertNotNull(inputEnvelope);
            }
        }
    }
}
