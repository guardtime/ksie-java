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

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.extending.EnvelopeSignatureExtender;
import com.guardtime.envelope.extending.ExtendedEnvelope;
import com.guardtime.envelope.extending.ksi.KsiEnvelopeSignatureExtendingPolicy;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.EnvelopeWriter;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.AnnotationMergingException;
import com.guardtime.envelope.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.envelope.packaging.exception.DocumentMergingException;
import com.guardtime.envelope.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.envelope.packaging.exception.ManifestMergingException;
import com.guardtime.envelope.packaging.exception.SignatureMergingException;
import com.guardtime.envelope.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.packaging.zip.ZipEnvelopeWriter;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.result.ResultHolder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EnvelopeMergingIntegrationTest extends AbstractCommonIntegrationTest {
    private EnvelopePackagingFactory incPackagingFactory;


    /**
     * Envelopes - for creating file conflicts when trying to merge envelopes.
     */
    private static final String[] ENVELOPES_FOR_DOCUMENTS_MANIFEST_CONFLICT =
            {"envelopes/documents-manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_ANNOTATION_DATA_CONFLICT =
            {"envelopes/annotation-data-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT =
            {"envelopes/annotation-manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_ANNOTATIONS_MANIFEST_CONFLICT =
            {"envelopes/annotations-manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_DOCUMENT_CONFLICT =
            {"envelopes/document-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_MANIFEST_CONFLICT =
            {"envelopes/manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_SIGNATURE_CONFLICT =
            {"envelopes/signature-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_FOR_MIX_CONFLICT_1 =
            {"envelopes/mix-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};

    /**
     * Envelopes - merging those envelope should not yield any exception.
     */
    private static final String[] ENVELOPES_FOR_SAME_DOCUMENT =
            {"envelopes/same-document-file.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] ENVELOPES_IDENTICAL =
            {"envelopes/multiple-annotations-copy.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};

    @Before
    public void setUp() throws Exception {
        super.setUp();
        packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .withParsingStore(parsingStore)
                .build();
        incPackagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withIndexProviderFactory(new IncrementingIndexProviderFactory())
                .withParsingStore(parsingStore)
                .build();
    }

    @Test
    public void testMergeParsedEnvelopeWithCreatedEnvelope() throws Exception {
        try (Envelope parsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope newEnvelope = packagingFactory.create(
                     singletonList(testDocumentHelloText),
                     new LinkedList<Annotation>()
             )) {
            int expectedSignatureContentsSize =
                    parsedEnvelope.getSignatureContents().size() + newEnvelope.getSignatureContents().size();
            parsedEnvelope.addAll(newEnvelope.getSignatureContents(), parsingStore);
            assertSignatureContentsCount(parsedEnvelope, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeParsedEnvelopeWithCreatedSignatureContent() throws Exception {
        try (Envelope parsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES)) {
            int expectedSignatureContentsSize =
                    parsedEnvelope.getSignatureContents().size() + 1;
            parsedEnvelope.add(createSignatureContent(), parsingStore);
            assertSignatureContentsCount(parsedEnvelope, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeParsedEnvelopeWithCreatedSignatureContentList() throws Exception {
        try (Envelope parsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES)) {
            List<SignatureContent> signatureContents = new LinkedList<>();
            signatureContents.add(createSignatureContent());
            signatureContents.add(createSignatureContent());
            signatureContents.add(createSignatureContent());
            int expectedSignatureContentsSize =
                    parsedEnvelope.getSignatureContents().size() + signatureContents.size();
            parsedEnvelope.addAll(signatureContents, parsingStore);
            assertSignatureContentsCount(parsedEnvelope, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeEnvelopesWithDifferentIndexProviders1() throws Exception {
        try (Envelope envelope1 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES);
             Envelope envelope2 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            envelope1.addAll(envelope2.getSignatureContents(), parsingStore);
            Assert.assertEquals(4, envelope1.getSignatureContents().size());
        }
    }

    @Test
    public void testMergeEnvelopesWithDifferentIndexProviders2() throws Exception {
        try (Envelope envelope1 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES);
             Envelope envelope2 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            envelope2.addAll(envelope1.getSignatureContents(), parsingStore);
            Assert.assertEquals(4, envelope2.getSignatureContents().size());
        }
    }

    @Test
    public void testAddNewContentToMergedEnvelope1() throws Exception {
        try (Document document = documentFactory.create(
                new ByteArrayInputStream("".getBytes()),
                "textDoc",
                "1-" + Long.toString(new Date().getTime())
            );
            Envelope uuidEnvelope = packagingFactory.create(singletonList(document), singletonList(stringEnvelopeAnnotation));
            Envelope incEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_INCREMENTING_INDEXES);
            Document document2 = documentFactory.create(
                    new ByteArrayInputStream("".getBytes()),
                    "textDoc",
                    "2-" + Long.toString(new Date().getTime())
            )
        ) {
            uuidEnvelope.addAll(incEnvelope.getSignatureContents(), parsingStore);
            try (Envelope second = packagingFactory.addSignature(
                    uuidEnvelope,
                    singletonList(document2),
                    singletonList(stringEnvelopeAnnotation)
            )) {
                assertEquals(second.getSignatureContents().size(), 4);
            }
        }
    }

    @Test
    public void testWritingMergedEnvelope_OK() throws Exception {
        try (Envelope parsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope secondParsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_INCREMENTING_INDEXES);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            int expectedSignatureContentCount =
                    parsedEnvelope.getSignatureContents().size() + secondParsedEnvelope.getSignatureContents().size();
            parsedEnvelope.addAll(secondParsedEnvelope.getSignatureContents(), parsingStore);
            envelopeWriter.write(parsedEnvelope, outputStream);
            assertNotNull(outputStream.toByteArray());
            assertTrue(outputStream.toByteArray().length > 0);
            try (Envelope mergedEnvelope = packagingFactory.read(new ByteArrayInputStream(outputStream.toByteArray()))) {
                assertEquals(expectedSignatureContentCount, mergedEnvelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddNewContentToMergedEnvelope2() throws Exception {
        try (Envelope uuidEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope incEnvelope = incPackagingFactory.create(
                     singletonList(testDocumentHelloText),
                     singletonList(stringEnvelopeAnnotation)
             );
             Document document = documentFactory.create(
                     new ByteArrayInputStream("".getBytes()),
                     "textDoc",
                     Long.toString(new Date().getTime())
             )
        ) {
            incEnvelope.addAll(uuidEnvelope.getSignatureContents(), parsingStore);
            try (Envelope second = incPackagingFactory.addSignature(
                    incEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            )) {
                assertEquals(second.getSignatureContents().size(), 3);
            }
        }
    }

    @Test
    public void testMergeEnvelopesDocumentManifestConflict() throws Exception {
        expectedException.expect(DocumentsManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing DocumentsManifest! Path: META-INF/datamanifest-1.tlv");
        mergeEnvelopes(ENVELOPES_FOR_DOCUMENTS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesAnnotationDataConflict() throws Exception {
        expectedException.expect(AnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-1.dat");
        mergeEnvelopes(ENVELOPES_FOR_ANNOTATION_DATA_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesSingleAnnotationManifestConflict() throws Exception {
        expectedException.expect(SingleAnnotationManifestMergingException.class);
        expectedException.expectMessage(
                "New SignatureContent has clashing SingleAnnotationManifest! Path: META-INF/annotation-1.tlv"
        );
        mergeEnvelopes(ENVELOPES_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesAnnotationsManifestConflict() throws Exception {
        expectedException.expect(AnnotationsManifestMergingException.class);
        expectedException.expectMessage(
                "New SignatureContent has clashing AnnotationsManifest! Path: META-INF/annotmanifest-1.tlv"
        );
        mergeEnvelopes(ENVELOPES_FOR_ANNOTATIONS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesSignatureConflict() throws Exception {
        expectedException.expect(SignatureMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing signature! Path: META-INF/signature-1.ksi");
        mergeEnvelopes(ENVELOPES_FOR_SIGNATURE_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesDocumentConflict() throws Exception {
        expectedException.expect(DocumentMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for Document!");
        mergeEnvelopes(ENVELOPES_FOR_DOCUMENT_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesManifestConflict() throws Exception {
        expectedException.expect(ManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Manifest! Path: META-INF/manifest-1.tlv");
        mergeEnvelopes(ENVELOPES_FOR_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesUnknownConflictsWithEnvelopeFile1() throws Exception {
        expectedException.expect(AnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeEnvelopes(ENVELOPES_FOR_MIX_CONFLICT_1);
    }

    @Test
    public void testMergeEnvelopesWithExactSameDocument() throws Exception {
        try (Envelope envelope = mergeEnvelopesUnclosed(ENVELOPES_FOR_SAME_DOCUMENT)) {
            assertEquals(2, envelope.getSignatureContents().size());
            assertSignatureContentsCount(envelope, 2);
        }
    }

    @Test
    public void testAddContentToVerifiedEnvelope() throws Exception {
        try (VerifiedEnvelope verifiedEnvelope =
                     new VerifiedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_RANDOM_UUID_INDEXES), new ResultHolder())) {
            addContent(verifiedEnvelope, 2);
        }
    }

    @Test
    public void testAddEnvelopeToVerifiedEnvelope() throws Exception {
        try (VerifiedEnvelope verifiedEnvelope =
                     new VerifiedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_RANDOM_UUID_INDEXES), new ResultHolder())) {
            addEnvelope(verifiedEnvelope, 2);
        }
    }

    @Test
    public void testAddAllContentsToVerifiedEnvelope() throws Exception {
        try (VerifiedEnvelope verifiedEnvelope =
                     new VerifiedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addAllContents(verifiedEnvelope, 4);
        }
    }

    @Test
    public void testAddContentToExtendedEnvelope() throws Exception {
        try (ExtendedEnvelope extendedEnvelope =
                     new ExtendedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_RANDOM_UUID_INDEXES))) {
            addContent(extendedEnvelope, 2);
        }
    }

    @Test
    public void testAddEnvelopeToExtendedEnvelope() throws Exception {
        try (ExtendedEnvelope extendedEnvelope =
                     new ExtendedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_RANDOM_UUID_INDEXES))) {
            addEnvelope(extendedEnvelope, 2);
        }
    }

    @Test
    public void testAddAllContentsToExtendedEnvelope() throws Exception {
        try (ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT))) {
            addAllContents(extendedEnvelope, 4);
        }
    }

    @Test
    public void testCheckMergingSourceEnvelopeContentCount() throws Exception {
        try (Envelope first = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope second = getEnvelope(ENVELOPE_WITH_ONE_DOCUMENT)) {
            first.addAll(second.getSignatureContents(), parsingStore);
            Assert.assertEquals(1, second.getSignatureContents().size());
        }
    }

    @Test
    public void testAddContentToMergingSourceEnvelope() throws Exception {
        try (Envelope first = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope second = getEnvelope(ENVELOPE_WITH_ONE_DOCUMENT)) {

            first.addAll(second.getSignatureContents(), parsingStore);
            try (Envelope third = packagingFactory.addSignature(
                    second,
                    singletonList(testDocumentHelloPdf),
                    singletonList(stringEnvelopeAnnotation)
            )) {
                assertEquals(2, third.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testMergeWithUnknownFiles() throws Exception {
        try (Envelope first = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope second  = getEnvelope(ENVELOPE_WITH_MULTIPLE_SIGNATURES)) {
            for (SignatureContent content : second.getSignatureContents()) {
                first.add(content, parsingStore);
            }
            assertEquals(3, first.getSignatureContents().size());
            assertEquals(2, second.getSignatureContents().size());
            assertEquals(0, first.getUnknownFiles().size());
            assertEquals(1, second.getUnknownFiles().size());
        }
    }

    @Test
    public void testMergeEnvelopeWithExactSameEnvelopes() throws Exception {
        try (Envelope envelope = mergeEnvelopesUnclosed(ENVELOPES_IDENTICAL)) {
            assertEquals(1, envelope.getSignatureContents().size());
            assertSignatureContentsCount(envelope, 1);
        }
    }

    @Test
    public void testMergeEnvelopeWithSameEnvelopesButDifferentSignature() throws Exception {
        expectedException.expect(SignatureMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing signature!");
        try (Envelope envelope1 = getEnvelope(ENVELOPE_WITH_MULTIPLE_ANNOTATIONS);
             Envelope envelope2 = getEnvelope(ENVELOPE_WITH_MULTIPLE_ANNOTATIONS)) {
            EnvelopeSignatureExtender ext = new EnvelopeSignatureExtender(signatureFactory,
                    new KsiEnvelopeSignatureExtendingPolicy(ksi));
            ExtendedEnvelope env = ext.extend(envelope2);
            envelope1.addAll(env.getSignatureContents(), parsingStore);
        }
    }

    @Test
    public void testMergingSplitEnvelopeWithSameAnnotationInMultipleSignatures() throws Exception {
        try (Envelope original =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation);
             Envelope first = new Envelope(original.getSignatureContents().get(0));
             Envelope second = new Envelope(original.getSignatureContents().get(1))) {
            first.addAll(second.getSignatureContents(), parsingStore);
            ByteArrayOutputStream baos  = new ByteArrayOutputStream();
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            writer.write(first, baos);
            try (Envelope envelope = packagingFactory.read(new ByteArrayInputStream(baos.toByteArray()))) {
                compareEnvelopeBytes(original, envelope, 2);
            }
        }
    }

    private void addContent(Envelope target, int expectedSize) throws Exception {
        try (Envelope source = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_NO_DOCUMENTS)) {
            target.add(source.getSignatureContents().get(0), parsingStore);
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void addAllContents(Envelope target, int expectedSize) throws Exception {
        try (Envelope source = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            target.addAll(source.getSignatureContents(), parsingStore);
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void addEnvelope(Envelope target, int expectedSize) throws Exception {
        try (Envelope source = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_NO_DOCUMENTS)) {
            target.addAll(source.getSignatureContents(), parsingStore);
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void mergeEnvelopes(String[] envelopes) throws Exception {
        mergeEnvelopesUnclosed(envelopes).close();
    }

    private Envelope mergeEnvelopesUnclosed(String[] envelopes) throws Exception {
        Envelope envelope1 = getEnvelope(envelopes[0]);
        try (Envelope envelope2 = getEnvelope(envelopes[1])) {
            for (SignatureContent content : envelope2.getSignatureContents()) {
                envelope1.add(content, parsingStore);
            }
        }
        return envelope1;
    }

    private SignatureContent createSignatureContent(Document existingDocument) throws Exception {
        Document document = existingDocument;
        if (document == null) {
            document =
                    documentFactory.create(
                            new ByteArrayInputStream(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)),
                            "text/plain",
                            UUID.randomUUID().toString()
                    );
        }
        try (Envelope temp = packagingFactory.create(singletonList(document), new LinkedList<Annotation>())) {
            return new SignatureContent(temp.getSignatureContents().get(0), parsingStore);
        }
    }

    private SignatureContent createSignatureContent() throws Exception {
        return createSignatureContent(null);
    }

    private void assertSignatureContentsCount(Envelope parsedEnvelope, int expectedSignatureContentsSize) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        envelopeWriter.write(parsedEnvelope, bos);
        try (Envelope envelopeFromBos = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
            assertEquals(expectedSignatureContentsSize, envelopeFromBos.getSignatureContents().size());
        }
    }

    private void compareEnvelopeBytes(Envelope first, Envelope second, int contentCount) throws IOException {
        assertEquals(contentCount, first.getSignatureContents().size());
        assertEquals(contentCount, second.getSignatureContents().size());
        EnvelopeWriter writer = new ZipEnvelopeWriter();
        ByteArrayOutputStream firstStream = new ByteArrayOutputStream();
        ByteArrayOutputStream secondStream = new ByteArrayOutputStream();
        writer.write(first, firstStream);
        writer.write(second, secondStream);
        assertTrue(Arrays.equals(firstStream.toByteArray(), secondStream.toByteArray()));
    }
}
