/*
 * Copyright 2013-2018 Guardtime, Inc.
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
import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.EnvelopeWriter;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.packaging.zip.ZipEnvelopeWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public abstract class AbstractEnvelopeParsingIntegrationTest extends AbstractCommonIntegrationTest {
    private EnvelopePackagingFactory packagingFactoryWithIncIndex;
    private EnvelopePackagingFactory packagingFactoryWithUuid;
    private EnvelopePackagingFactory defaultPackagingFactory;
    private ParsingStore parsingStore;
    private EnvelopeWriter envelopeWriter = new ZipEnvelopeWriter();


    protected abstract ParsingStore getParsingStore();

    protected EnvelopePackagingFactory getDefaultPackagingFactory() {
        return defaultPackagingFactory;
    }

    @Before
    public void setUpPackagingFactories() throws Exception {
        parsingStore = getParsingStore();
        this.packagingFactoryWithIncIndex = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withIndexProviderFactory(new IncrementingIndexProviderFactory())
                .withParsingStoreFactory(parsingStore)
                .build();
        this.packagingFactoryWithUuid = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .withParsingStoreFactory(parsingStore)
                .build();
        this.defaultPackagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withParsingStoreFactory(parsingStore)
                .build();
    }

    @Test
    public void testReadEnvelopeWithMissingManifest() throws Exception {
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("Reading envelope encountered errors!");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MISSING_MANIFEST)) {
            //empty
        }
    }

    @Test
    public void testReadEnvelopeWithMissingMimetype() throws Exception {
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("No parsable MIME type.");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MISSING_MIMETYPE)) {
            //empty
        }
    }

    @Test
    public void testReadEnvelopeWithEmptyMimetype() throws Exception {
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("Parsed Envelope has invalid MIME type. Can't process it!");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MIMETYPE_IS_EMPTY)) {
            //empty
        }
    }

    @Test
    public void testReadEnvelopeWithMissingDocumentManifest() throws Exception {
        expectedException.expect(EnvelopeReadingException.class);
        expectedException.expectMessage("Reading envelope encountered errors!");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MISSING_DOCUMENTS_MANIFEST)) {
            //empty
        }
    }

    @Test
    public void testReadEnvelopeWithMissingAnnotationsManifest() throws Exception {
        expectedException.expect(EnvelopeReadingException.class);
        expectedException.expectMessage("Reading envelope encountered errors!");
        try (Envelope ignored = getEnvelope(ENVELOPE_WITH_MISSING_ANNOTATIONS_MANIFEST)) {
            //empty
        }
    }

    @Test
    public void testCreateEnvelope() throws Exception {
        try (
                Envelope envelope = new EnvelopeBuilder(defaultPackagingFactory)
                        .withDocument(
                                new ByteArrayInputStream("Test_Data".getBytes(StandardCharsets.UTF_8)),
                                TEST_FILE_NAME_TEST_TXT,
                                "application/txt"
                        )
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
                        .withDocument(
                                new ByteArrayInputStream("Test_Data".getBytes(StandardCharsets.UTF_8)),
                                TEST_FILE_NAME_TEST_TXT,
                                "application/txt"
                        )
                        .build()
            ) {
                envelopeWriter.write(envelope, bos);

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
        try (Envelope envelope = packagingFactoryWithUuid.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadEnvelopeWithDifferentIndexProviderCombination2_OK() throws Exception {
        try (Envelope envelope = packagingFactoryWithIncIndex.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexProviderCombination_OK() throws Exception {
        try (Envelope existingEnvelope = packagingFactoryWithIncIndex.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
            packagingFactoryWithUuid.addSignature(
                    existingEnvelope,
                    singletonList(testDocumentHelloPdf),
                    singletonList(stringEnvelopeAnnotation)
            );
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexProviderCombination2_OK() throws Exception {
        try (Envelope existingEnvelope = packagingFactoryWithUuid.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
                packagingFactoryWithIncIndex.addSignature(
                        existingEnvelope,
                        singletonList(testDocumentHelloPdf),
                        singletonList(stringEnvelopeAnnotation)
                );
                writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadEnvelopeWithRandomIncrementingIndexesAndAddNewContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_RANDOM_INCREMENTING_INDEXES));
                Envelope existingEnvelope = defaultPackagingFactory.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new ParsedDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            defaultPackagingFactory.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testReadEnvelopeWithRandomUuidIndexesAndAddNewContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_RANDOM_UUID_INDEXES));
                Envelope existingEnvelope = packagingFactoryWithUuid.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new ParsedDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            packagingFactoryWithUuid.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexTypesInSameContent_OK() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES));
                Envelope existingEnvelope = packagingFactoryWithUuid.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new ParsedDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            packagingFactoryWithUuid.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithUuid);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingWithDifferentIndexesInContents_OK() throws Exception {
        try (
                Envelope existingEnvelope =
                        packagingFactoryWithIncIndex.read(
                                new FileInputStream(loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS))
                        );
                Document document = new ParsedDocument(
                        new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                        MIME_TYPE_APPLICATION_TXT,
                        "Doc.doc"
        )) {
            packagingFactoryWithUuid.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation));
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testAddDocumentsToExistingEnvelopeWithOneContentRemoved_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("File name is not valid! File name: META-INF/");
        try (
                Envelope existingEnvelope = packagingFactoryWithIncIndex.read(
                        new FileInputStream(loadFile(ENVELOPE_WITH_TWO_CONTENTS_AND_ONE_MANIFEST_REMOVED))
                );
                Document document = new ParsedDocument(
                        new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                        MIME_TYPE_APPLICATION_TXT,
                        "Doc.doc"
                );
                Annotation envelopeAnnotation =
                        new StringAnnotation(
                                "annotation 101",
                                "com.guardtime",
                                EnvelopeAnnotationType.FULLY_REMOVABLE
                        )
        ) {
            packagingFactoryWithIncIndex.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(envelopeAnnotation)
            );
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testAddDocumentsToExistingEnvelopeUnknownFiles_OK() throws Exception {
        try (
                Envelope existingEnvelope =
                        packagingFactoryWithIncIndex.read(new FileInputStream(loadFile(ENVELOPE_WITH_UNKNOWN_FILES)));
                Document document = new ParsedDocument(
                        new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                        MIME_TYPE_APPLICATION_TXT,
                        "Doc.doc"
                );
                Annotation envelopeAnnotation = new StringAnnotation(
                        "annotation 101",
                        "com.guardtime",
                        EnvelopeAnnotationType.FULLY_REMOVABLE
                )
        ) {
            packagingFactoryWithIncIndex.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(envelopeAnnotation)
            );
            writeEnvelopeToAndReadFromStream(existingEnvelope, packagingFactoryWithIncIndex);
        }
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesManifestUri_throwsIllegalArgumentException() throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/manifest-1.tlv");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesDocumentManifestUri_throwsIllegalArgumentException()
            throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/datamanifest-1.tlv");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesAnnotationsManifestUri_throwsIllegalArgumentException()
            throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/annotmanifest-1.tlv");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesSingleAnnotationManifestUri_throwsIllegalArgumentException()
            throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/annotation-1.tlv");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesAnnotationUri_throwsIllegalArgumentException() throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/annotation-1.dat");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesMimeTypeUri_throwsIllegalArgumentException() throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("mimetype");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesSignatureUri_throwsIllegalArgumentException() throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/signature-1.ksi");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentIsWrittenAsMetaInfDirectory_throwsIllegalArgumentException() throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF/");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentFileUriMatchesMetaInfDirectoryName_throwsIllegalArgumentException()
            throws Exception {
        createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation("META-INF");
    }

    @Test
    public void testCreateEnvelopeWhereDocumentIsWrittenAsDirectory_throwsIOException() throws Exception {
        String documentFileName = "SubDir/";
        expectedException.expect(IOException.class);
        expectedException.expectMessage(documentFileName + " is an invalid document file name!");
        createEnvelopeWriteItToAndReadFromStream(documentFileName);
    }

    @Test
    public void testCreateEnvelopeWhereDocumentIsWrittenToSubDirectory_Ok() throws Exception {
        createEnvelopeWriteItToAndReadFromStream("SubDir/AddedDocument.txt");
    }

    @Test
    public void testReadAndWriteMangledEnvelope() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Aborting Envelope writing.");
        try (
                InputStream inputStream = new FileInputStream(loadFile(ENVELOPE_WITH_BROKEN_SIGNATURE_CONTENT));
                Envelope ignored = defaultPackagingFactory.read(inputStream)
        ) {
            //Empty
        } catch (EnvelopeReadingException e) {
            try (Envelope envelope = e.getEnvelope()) {
                Assert.assertFalse(envelope.getSignatureContents().isEmpty());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                envelopeWriter.write(envelope, bos);
            }
        }
    }

    private List<Document> getEnvelopeDocument(String fileName) {
        return singletonList((Document) new ParsedDocument(
                new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                MIME_TYPE_APPLICATION_TXT,
                fileName
        ));
    }

    private void assertSingleContentsWithSingleDocumentWithName(Envelope envelope, String testFileName) {
        List<? extends SignatureContent> contents = envelope.getSignatureContents();
        Assert.assertNotNull(contents);
        Assert.assertEquals(1, contents.size());

        SignatureContent content = contents.get(0);
        Assert.assertNotNull(content);
        Map<String, Document> documents = content.getDocuments();
        Assert.assertEquals(1, documents.size());
        if (testFileName != null) {
            Assert.assertNotNull(documents.get(testFileName));
        }
    }

    private void assertSingleContentsWithSingleDocument(Envelope envelope) {
        assertSingleContentsWithSingleDocumentWithName(envelope, null);
    }

    /*
    Created envelope will be closed in the end.
     */
    private void createEnvelopeWriteItToAndReadFromStreamWithExceptionExpectation(String documentFileName) throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("File name is not valid! File name: " + documentFileName);
        createEnvelopeWriteItToAndReadFromStream(documentFileName);
    }

    private void createEnvelopeWriteItToAndReadFromStream(String documentFileName) throws Exception {
        try (Envelope envelope = defaultPackagingFactory.create(
                getEnvelopeDocument(documentFileName),
                singletonList(stringEnvelopeAnnotation)
        )) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactoryWithIncIndex);
        }
    }
}
