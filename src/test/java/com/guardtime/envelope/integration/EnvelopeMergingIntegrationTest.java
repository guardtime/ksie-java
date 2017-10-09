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

import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.extending.ExtendedEnvelope;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.envelope.packaging.exception.EnvelopeAnnotationMergingException;
import com.guardtime.envelope.packaging.exception.DocumentMergingException;
import com.guardtime.envelope.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.envelope.packaging.exception.ManifestMergingException;
import com.guardtime.envelope.packaging.exception.SignatureMergingException;
import com.guardtime.envelope.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.result.ResultHolder;

import org.junit.Assert;
import org.junit.Before;
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

public class EnvelopeMergingIntegrationTest extends AbstractCommonIntegrationTest {
    private EnvelopePackagingFactory incPackagingFactory;


    /**
     * Envelopes - for creating file conflicts when trying to merge envelopes.
     */
    private static final String[] CONTAINERS_FOR_UNKNOWN_FILE_CONFLICT = {"envelopes/unknown-file-conflict.ksie", ENVELOPE_WITH_UNKNOWN_FILES};
    private static final String[] CONTAINERS_FOR_DOCUMENTS_MANIFEST_CONFLICT = {"envelopes/documents-manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_ANNOTATION_DATA_CONFLICT = {"envelopes/annotation-data-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT = {"envelopes/annotation-manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_ANNOTATIONS_MANIFEST_CONFLICT = {"envelopes/annotations-manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_DOCUMENT_CONFLICT = {"envelopes/document-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_MANIFEST_CONFLICT = {"envelopes/manifest-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_SIGNATURE_CONFLICT = {"envelopes/signature-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_MIX_CONFLICT_1 = {"envelopes/mix-conflict.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_FOR_MIX_CONFLICT_2 = {ENVELOPE_WITH_MULTIPLE_ANNOTATIONS, "envelopes/mix-conflict.ksie"};

    /**
     * Envelopes - merging those envelope should not yield any exception.
     */
    private static final String[] CONTAINERS_FOR_SAME_DOCUMENT = {"envelopes/same-document-file.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};
    private static final String[] CONTAINERS_IDENTICAL = {"envelopes/multiple-annotations-copy.ksie", ENVELOPE_WITH_MULTIPLE_ANNOTATIONS};

    @Before
    public void setUp() throws Exception {
        super.setUp();
        packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        incPackagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                build();
    }

    @Test
    public void testMergeParsedEnvelopeWithCreatedEnvelope() throws Exception {
        try (Envelope parsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES);
             Envelope newEnvelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new LinkedList<EnvelopeAnnotation>())) {
            int expectedSignatureContentsSize =
                    parsedEnvelope.getSignatureContents().size() + newEnvelope.getSignatureContents().size();
            parsedEnvelope.add(newEnvelope);
            assertSignatureContentsCount(parsedEnvelope, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeParsedEnvelopeWithCreatedSignatureContent() throws Exception {
        try (Envelope parsedEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_UUID_INDEXES)) {
            int expectedSignatureContentsSize =
                    parsedEnvelope.getSignatureContents().size() + 1;
            parsedEnvelope.add(createSignatureContent());
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
            parsedEnvelope.addAll(signatureContents);
            assertSignatureContentsCount(parsedEnvelope, expectedSignatureContentsSize);
        }
    }

    @Test
    public void testMergeEnvelopesWithDifferentIndexProviders1() throws Exception {
        try (Envelope envelope1 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES);
             Envelope envelope2 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            envelope1.add(envelope2);
            Assert.assertEquals(4, envelope1.getSignatureContents().size());
        }
    }

    @Test
    public void testMergeEnvelopesWithDifferentIndexProviders2() throws Exception {
        try (Envelope envelope1 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES);
             Envelope envelope2 = getEnvelope(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            envelope2.add(envelope1);
            Assert.assertEquals(4, envelope2.getSignatureContents().size());
        }
    }

    @Test
    public void testAddNewContentToMergedEnvelope1() throws Exception {
        try (EnvelopeDocument document = new StreamEnvelopeDocument(new ByteArrayInputStream("".getBytes()), "textDoc", "1-" + Long.toString(new Date().getTime()));
             Envelope uuidEnvelope = packagingFactory.create(singletonList(document), singletonList(STRING_ENVELOPE_ANNOTATION));
             Envelope incEnvelope = getEnvelope(ENVELOPE_WITH_RANDOM_INCREMENTING_INDEXES);
             EnvelopeDocument document2 = new StreamEnvelopeDocument(new ByteArrayInputStream("".getBytes()), "textDoc", "2-" + Long.toString(new Date().getTime()))) {
            uuidEnvelope.add(incEnvelope);
            packagingFactory.addSignature(uuidEnvelope, singletonList(document2), singletonList(STRING_ENVELOPE_ANNOTATION));
            assertEquals(uuidEnvelope.getSignatureContents().size(), 4);
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
            parsedEnvelope.add(secondParsedEnvelope);
            parsedEnvelope.writeTo(outputStream);
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
             Envelope incEnvelope = incPackagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), singletonList(STRING_ENVELOPE_ANNOTATION));
             EnvelopeDocument document = new StreamEnvelopeDocument(new ByteArrayInputStream("".getBytes()), "textDoc", Long.toString(new Date().getTime()))) {
            incEnvelope.add(uuidEnvelope);
            incPackagingFactory.addSignature(incEnvelope, singletonList(document), singletonList(STRING_ENVELOPE_ANNOTATION));
            assertEquals(incEnvelope.getSignatureContents().size(), 3);
        }
    }

    @Test
    public void testMergeEnvelopesUnknownFileConflict() throws Exception {
        expectedException.expect(DocumentMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for EnvelopeDocument! Path: META-INF/sun.txt");
        mergeEnvelopes(CONTAINERS_FOR_UNKNOWN_FILE_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesDocumentManifestConflict() throws Exception {
        expectedException.expect(DocumentsManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing DocumentsManifest! Path: META-INF/datamanifest-1.tlv");
        mergeEnvelopes(CONTAINERS_FOR_DOCUMENTS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesAnnotationDataConflict() throws Exception {
        expectedException.expect(EnvelopeAnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-1.dat");
        mergeEnvelopes(CONTAINERS_FOR_ANNOTATION_DATA_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesSingleAnnotationManifestConflict() throws Exception {
        expectedException.expect(SingleAnnotationManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing SingleAnnotationManifest! Path: META-INF/annotation-1.tlv");
        mergeEnvelopes(CONTAINERS_FOR_SINGLE_ANNOTATION_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesAnnotationsManifestConflict() throws Exception {
        expectedException.expect(AnnotationsManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing AnnotationsManifest! Path: META-INF/annotmanifest-1.tlv");
        mergeEnvelopes(CONTAINERS_FOR_ANNOTATIONS_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesSignatureConflict() throws Exception {
        expectedException.expect(SignatureMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing signature! Path: META-INF/signature-1.ksi");
        mergeEnvelopes(CONTAINERS_FOR_SIGNATURE_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesDocumentConflict() throws Exception {
        expectedException.expect(DocumentMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for EnvelopeDocument!");
        mergeEnvelopes(CONTAINERS_FOR_DOCUMENT_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesManifestConflict() throws Exception {
        expectedException.expect(ManifestMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Manifest! Path: META-INF/manifest-1.tlv");
        mergeEnvelopes(CONTAINERS_FOR_MANIFEST_CONFLICT);
    }

    @Test
    public void testMergeEnvelopesUnknownConflictsWithEnvelopeFile1() throws Exception {
        expectedException.expect(EnvelopeAnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeEnvelopes(CONTAINERS_FOR_MIX_CONFLICT_1);
    }

    @Test
    public void testMergeEnvelopesUnknownConflictsWithEnvelopeFile2() throws Exception {
        expectedException.expect(EnvelopeAnnotationMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Annotation data! Path: META-INF/annotation-2.dat");
        mergeEnvelopes(CONTAINERS_FOR_MIX_CONFLICT_2);
    }

    @Test
    public void testMergeEnvelopesWithExactSameDocument() throws Exception {
        try (Envelope envelope = mergeEnvelopesUnclosed(CONTAINERS_FOR_SAME_DOCUMENT)) {
            assertEquals(2, envelope.getSignatureContents().size());
        }
    }

    @Test
    public void testMergeEnvelopeWithExactSameEnvelope() throws Exception {
        try (Envelope envelope = mergeEnvelopesUnclosed(CONTAINERS_IDENTICAL)) {
            assertEquals(2, envelope.getSignatureContents().size());
            assertSignatureContentsCount(envelope, 1);
        }
    }

    @Test
    public void testAddContentToVerifiedEnvelope() throws Exception {
        try (VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addContent(verifiedEnvelope, 2);
        }
    }

    @Test
    public void testAddEnvelopeToVerifiedEnvelope() throws Exception {
        try (VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addEnvelope(verifiedEnvelope, 2);
        }
    }

    @Test
    public void testAddAllContentsToVerifiedEnvelope() throws Exception {
        try (VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT), new ResultHolder())) {
            addAllContents(verifiedEnvelope, 4);
        }
    }

    @Test
    public void testAddContentToExtendedEnvelope() throws Exception {
        try (ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT))) {
            addContent(extendedEnvelope, 2);
        }
    }

    @Test
    public void testAddEnvelopeToExtendedEnvelope() throws Exception {
        try (ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT))) {
            addEnvelope(extendedEnvelope, 2);
        }
    }

    @Test
    public void testAddAllContentsToExtendedEnvelope() throws Exception {
        try (ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT))) {
            addAllContents(extendedEnvelope, 4);
        }
    }

    private void addContent(Envelope target, int expectedSize) throws Exception {
        try (Envelope source = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_NO_DOCUMENTS)) {
            target.add(source.getSignatureContents().get(0));
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void addAllContents(Envelope target, int expectedSize) throws Exception {
        try (Envelope source = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS)) {
            target.addAll(source.getSignatureContents());
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void addEnvelope(Envelope target, int expectedSize) throws Exception {
        try (Envelope source = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_NO_DOCUMENTS)) {
            target.add(source);
            assertEquals(expectedSize, target.getSignatureContents().size());
        }
    }

    private void mergeEnvelopes(String[] envelopes) throws Exception {
        mergeEnvelopesUnclosed(envelopes).close();
    }

    private Envelope mergeEnvelopesUnclosed(String[] envelopes) throws Exception {
        Envelope envelope1 = getEnvelope(envelopes[0]);
        try (Envelope envelope2 = getEnvelope(envelopes[1])) {
            envelope1.add(envelope2);
        }
        return envelope1;
    }

    private SignatureContent createSignatureContent(EnvelopeDocument existingDocument) throws Exception {
        EnvelopeDocument envelopeDocument = existingDocument;
        if (envelopeDocument == null) {
            envelopeDocument = new StreamEnvelopeDocument(
                    new ByteArrayInputStream(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)),
                    "text/plain",
                    UUID.randomUUID().toString()
            );
        }
        try (Envelope temp = packagingFactory.create(singletonList(envelopeDocument), new LinkedList<EnvelopeAnnotation>())) {
            return temp.getSignatureContents().get(0);
        }
    }

    private SignatureContent createSignatureContent() throws Exception {
        return createSignatureContent(null);
    }

    private void assertSignatureContentsCount(Envelope parsedEnvelope, int expectedSignatureContentsSize) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        parsedEnvelope.writeTo(bos);
        try (Envelope envelopeFromBos = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
            assertEquals(expectedSignatureContentsSize, envelopeFromBos.getSignatureContents().size());
        }
    }
}
