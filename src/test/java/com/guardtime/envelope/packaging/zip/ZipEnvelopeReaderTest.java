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

package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.tlv.TlvEnvelopeManifestFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.exception.InvalidPackageException;
import com.guardtime.envelope.packaging.parsing.store.TemporaryFileBasedParsingStoreFactory;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZipEnvelopeReaderTest extends AbstractEnvelopeTest {

    @Mock
    private KSI mockKsi;

    private ZipEnvelopeReader reader;
    private Envelope envelope;
    private List<Throwable> exceptions;


    @Before
    public void setUpReader() throws Exception {
        EnvelopeManifestFactory manifestFactory = new TlvEnvelopeManifestFactory();

        when(mockKsi.sign(any(DataHash.class))).thenReturn(mock(KSISignature.class));
        when(mockKsi.extend(any(KSISignature.class))).thenReturn(mock(KSISignature.class));
        KSISignature mockKsiSignature = mock(KSISignature.class);
        when(mockKsiSignature.getAggregationTime()).thenReturn(mock(Date.class));
        when(mockKsi.read(any(InputStream.class))).thenReturn(mockKsiSignature);
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        this.reader = new ZipEnvelopeReader(manifestFactory, signatureFactory, new TemporaryFileBasedParsingStoreFactory());
    }

    @After
    public void closeEnvelope() throws Exception {
        if (envelope != null) {
            envelope.close();
        }
    }

    private void setUpEnvelope(String path) throws Exception {
        try (InputStream input = new FileInputStream(loadFile(path))) {
            this.envelope = reader.read(input);
        } catch (EnvelopeReadingException e) {
            this.envelope = e.getEnvelope();
            this.exceptions = e.getExceptions();
        }
    }

    private void assertExceptionsContainMessage(String message) {
        String found = "";
        for (Throwable t : exceptions) {
            found = t.getMessage();
            if (found.equals(message)) {
                break;
            }
        }
        assertEquals(message, found);
    }

    @Test
    public void testReadEnvelopeFileWithDocument() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_ONE_DOCUMENT);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        for (SignatureContent content : envelope.getSignatureContents()) {
            assertFalse(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadEmptyEnvelopeFile_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("No parsable MIME type");
        setUpEnvelope(EMPTY_ENVELOPE);
    }

    @Test
    public void testReadEnvelopeWithMimetypeContainingInvalidValue_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Parsed Envelope has invalid MIME type. Can't process it!");
        setUpEnvelope(ENVELOPE_WITH_MIMETYPE_CONTAINS_INVALID_VALUE);
    }

    @Test
    public void testReadEnvelopeWithMimetypeContainingMoreThanNeeded_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Parsed Envelope has invalid MIME type. Can't process it!");
        setUpEnvelope(ENVELOPE_WITH_MIMETYPE_CONTAINS_ADDITIONAL_VALUE);
    }

    @Test
    public void testReadEnvelopeFileWithExtraFiles() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_UNKNOWN_FILES);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        assertFalse(envelope.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadEnvelopeFileWithoutDocuments() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_NO_DOCUMENTS);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        for (SignatureContent content : envelope.getSignatureContents()) {
            for (Document document : content.getDocuments().values()) {
                assertTrue(document instanceof EmptyDocument);
            }
        }
    }

    @Test
    public void testReadEnvelopeFileWithMissingDocumentUri() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_NO_DOCUMENT_URI_IN_MANIFEST);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        for (SignatureContent content : envelope.getSignatureContents()) {
            assertTrue(content.getDocuments().isEmpty());
            assertFalse(content.getDocumentsManifest().getDocumentReferences().isEmpty());
        }
    }

    @Test
    public void testReadEnvelopeFileWithMissingDocumentMIMEType() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_DOCUMENT_MISSING_MIMETYPE);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        for (SignatureContent content : envelope.getSignatureContents()) {
            assertTrue(content.getDocuments().isEmpty());
        }
    }

    @Test
    public void testReadEnvelopeFileWithMultipleAnnotations() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_MULTIPLE_ANNOTATIONS);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        for (SignatureContent content : envelope.getSignatureContents()) {
            assertTrue(content.getAnnotations().size() > 1);
        }
    }

    @Test
    public void testReadEnvelopeFileWithInvalidAnnotationType() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_INVALID_ANNOTATION_TYPE);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        for (SignatureContent content : envelope.getSignatureContents()) {
            AnnotationsManifest annotationsManifest = content.getAnnotationsManifest();
            int insertedAnnotationsCount = annotationsManifest.getSingleAnnotationManifestReferences().size();
            int parsableAnnotationsCount = content.getAnnotations().size();
            assertTrue(parsableAnnotationsCount < insertedAnnotationsCount);
        }
    }

    @Test
    public void testReadEnvelopeFileWithMultipleSignatures() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_MULTIPLE_SIGNATURES);
        assertNotNull(envelope);
        assertTrue(envelope.getSignatureContents().size() > 1);
    }

    @Test
    public void testReadEnvelopeFileWithBrokenSignatures() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_BROKEN_SIGNATURE_CONTENT);
        assertNotNull(envelope);
        assertFalse(envelope.getSignatureContents().isEmpty());
        assertFalse(envelope.getUnknownFiles().isEmpty());
    }

    @Test
    public void testReadEnvelopeFileWithMissingAnnotationData() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_MISSING_ANNOTATION_DATA);
        assertNotNull(envelope);
        SignatureContent signatureContent = envelope.getSignatureContents().get(0);
        AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest();
        assertFalse(envelope.getSignatureContents().isEmpty());
        assertTrue(signatureContent.getAnnotations().isEmpty());
        assertFalse(signatureContent.getSingleAnnotationManifests().isEmpty());
        assertFalse(annotationsManifest.getSingleAnnotationManifestReferences().isEmpty());
    }

    @Test
    public void testReadEnvelopeFileWithMissingAnnotation() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_MISSING_ANNOTATION);
        assertNotNull(envelope);
        SignatureContent signatureContent = envelope.getSignatureContents().get(0);
        AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest();
        assertFalse(envelope.getSignatureContents().isEmpty());
        assertTrue(signatureContent.getAnnotations().isEmpty());
        assertTrue(signatureContent.getSingleAnnotationManifests().isEmpty());
        assertFalse(annotationsManifest.getSingleAnnotationManifestReferences().isEmpty());
    }

    @Test
    public void testReadInvalidDocumentsManifest() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_MISSING_DOCUMENTS_MANIFEST);
        assertFalse(exceptions.isEmpty());
        assertExceptionsContainMessage("Failed to fetch file 'META-INF/datamanifest-4.tlv' from parsingStore.");
    }

    @Test
    public void testReadInvalidAnnotationsManifest() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_BROKEN_SIGNATURE_CONTENT);
        assertExceptionsContainMessage("Failed to fetch file 'META-INF/annotmanifest-2.tlv' from parsingStore.");
    }


    @Test
    public void testReadInvalidSingleAnnotationManifest() throws Exception {
        setUpEnvelope(ENVELOPE_WITH_MISSING_ANNOTATION);
        assertExceptionsContainMessage("Failed to fetch file 'META-INF/annotation-1.tlv' from parsingStore.");
    }

    @Test
    public void testReadInvalidSignature() throws Exception {
        when(mockKsi.read(any(InputStream.class))).thenThrow(SignatureException.class);
        setUpEnvelope(ENVELOPE_WITH_BROKEN_SIGNATURE_CONTENT);
        assertExceptionsContainMessage("Failed to parse content of 'META-INF/signature-1.ksi'");
    }


    @Test
    public void testReadInvalidEnvelopeProducesMultipleExceptions() throws Exception {
        when(mockKsi.read(any(InputStream.class))).thenThrow(SignatureException.class);
        setUpEnvelope(ENVELOPE_WITH_BROKEN_SIGNATURE_CONTENT);
        assertExceptionsContainMessage("Failed to parse content of 'META-INF/signature-1.ksi'");
        assertExceptionsContainMessage("Failed to parse content of 'META-INF/signature-2.ksi'");
        assertExceptionsContainMessage("Failed to fetch file 'META-INF/annotation-1.tlv' from parsingStore.");
        assertExceptionsContainMessage("Failed to fetch file 'META-INF/annotation-2.tlv' from parsingStore.");
        assertExceptionsContainMessage("Failed to fetch file 'META-INF/annotmanifest-2.tlv' from parsingStore.");
    }

}