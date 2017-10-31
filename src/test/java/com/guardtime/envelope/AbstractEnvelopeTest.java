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

package com.guardtime.envelope;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.annotation.StringAnnotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.StreamDocument;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.indexing.IndexProviderFactory;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.ManifestFactoryType;
import com.guardtime.envelope.manifest.SignatureReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.verification.rule.state.DefaultRuleStateProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractEnvelopeTest {

    /**
     * Envelopes - Internally correct and does verify against anchors.
     */
    protected static final String ENVELOPE_WITH_NO_DOCUMENTS = "envelopes/no-documents.ksie";
    protected static final String ENVELOPE_WITH_ONE_DOCUMENT = "envelopes/one-document.ksie";
    protected static final String ENVELOPE_WITH_UNKNOWN_FILES = "envelopes/unknown-files.ksie";
    protected static final String ENVELOPE_WITH_MULTIPLE_SIGNATURES = "envelopes/multiple-signatures.ksie";
    protected static final String ENVELOPE_WITH_RANDOM_UUID_INDEXES = "envelopes/random-uuid-indexes.ksie";
    protected static final String ENVELOPE_WITH_MULTIPLE_ANNOTATIONS = "envelopes/multiple-annotations.ksie";
    protected static final String ENVELOPE_WITH_MIXED_INDEX_TYPES = "envelopes/with-mixed-index-types.ksie";
    protected static final String ENVELOPE_WITH_NON_REMOVABLE_ANNOTATION = "envelopes/non-removable-annotation.ksie";
    protected static final String ENVELOPE_WITH_RANDOM_INCREMENTING_INDEXES = "envelopes/multi-content-random-incrementing-indexes.ksie";
    protected static final String ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS = "envelopes/contents-with-different-index-types.ksie";

    /**
     * Envelopes - Internally invalid or does not verify against anchors.
     */
    protected static final String EMPTY_ENVELOPE = "envelopes/invalid/empty.ksie";
    protected static final String ENVELOPE_WITH_MISSING_MANIFEST = "envelopes/invalid/missing-manifest.ksie";
    protected static final String ENVELOPE_WITH_MISSING_MIMETYPE = "envelopes/invalid/missing-mimetype.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_DOCUMENT = "envelopes/invalid/changed-document.ksie";
    protected static final String ENVELOPE_WITH_MISSING_SIGNATURE = "envelopes/invalid/missing-signature.ksie";
    protected static final String ENVELOPE_WITH_MIMETYPE_IS_EMPTY = "envelopes/invalid/mimetype-is-empty.ksie";
    protected static final String ENVELOPE_WITH_MISSING_ANNOTATION = "envelopes/invalid/missing-annotation.ksie";
    protected static final String ENVELOPE_WITH_WRONG_SIGNATURE_FILE = "envelopes/invalid/wrong-signature-file.ksie";
    protected static final String ENVELOPE_WITH_MISSING_DOCUMENTS_MANIFEST = "envelopes/missing-datamanifest.ksie";
    protected static final String ENVELOPE_WITH_CONTAINS_ONLY_MANIFEST = "envelopes/invalid/contains-only-manifest.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_ANNOTATION_DATA = "envelopes/invalid/changed-annotation-data.ksie";
    protected static final String ENVELOPE_WITH_MISSING_ANNOTATION_DATA = "envelopes/invalid/missing-annotation-data.ksie";
    protected static final String ENVELOPE_WITH_INVALID_ANNOTATION_TYPE = "envelopes/invalid/invalid-annotation-type.ksie";
    protected static final String ENVELOPE_WITH_BROKEN_SIGNATURE_CONTENT = "envelopes/invalid/broken-signature-content.ksie";
    protected static final String ENVELOPE_WITH_DOCUMENT_MISSING_MIMETYPE = "envelopes/invalid/document-missing-mimetype.ksie";
    protected static final String ENVELOPE_WITH_NO_DOCUMENT_URI_IN_MANIFEST = "envelopes/invalid/no-document-uri-in-manifest.ksie";
    protected static final String ENVELOPE_WITH_MIMETYPE_CONTAINS_INVALID_VALUE = "envelopes/invalid/mimetype-contains-invalid-value.ksie";
    protected static final String ENVELOPE_WITH_MULTIPLE_EXTENDABLE_SIGNATURES = "envelopes/invalid/multiple-signatures-non-verifying.ksie";
    protected static final String ENVELOPE_WITH_MIMETYPE_CONTAINS_ADDITIONAL_VALUE = "envelopes/invalid/mimetype-contains-additional-value.ksie";
    protected static final String ENVELOPE_WITH_TWO_CONTENTS_AND_ONE_MANIFEST_REMOVED = "envelopes/invalid/two-contents-one-manifest-removed.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_SIGNATURE_FILE = "envelopes/invalid/invalid-signature-from-last-aggregation-hash-chain.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_DATAMANIFEST_HASH_IN_MANIFEST = "envelopes/invalid/changed-datamanifest-hash-in-manifest.ksie";
    protected static final String ENVELOPE_WITH_MULTI_CONTENT_ONE_SIGNATURE_IS_INVALID = "envelopes/invalid/multi-content-one-signature-is-invalid.ksie";
    protected static final String ENVELOPE_WITH_MULTI_CONTENT_ONE_IS_MISSING_DATAMANIFEST = "envelopes/invalid/multi-content-one-content-is-missing-datamanifest.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_ANNOTATIONS_MANIFEST_HASH_IN_MANIFEST = "envelopes/invalid/changed-annotations-manifest-hash-in-manifest.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_AND_EXTENDED_SIGNATURE_FILE = "envelopes/invalid/invalid-signature-from-last-aggregation-hash-chain-extended.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST = "envelopes/invalid/changed-datamanifest-hash-in-annotation-manifest.ksie";
    protected static final String ENVELOPE_WITH_INVALID_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST = "envelopes/invalid/invalid-datamanifest-in-single-annotation-manifest.ksie";
    protected static final String ENVELOPE_WITH_CHANGED_ANNOTATION_MANIFEST_HASH_IN_ANNOTATIONS_MANIFEST = "envelopes/invalid/changed-annotation-manifest-hash-in-annotations-manifest.ksie";

    protected static final String MIME_TYPE_APPLICATION_TXT = "application/txt";
    protected static final String MIME_TYPE_APPLICATION_PDF = "application/pdf";
    protected static final String SIGNATURE_MIME_TYPE = "application/ksi-signature";
    protected static final String TEST_FILE_PATH_TEST_TXT = "test-data-files/test.txt";
    protected static final String TEST_FILE_NAME_TEST_DOC = "test.doc";
    protected static final String TEST_FILE_NAME_TEST_TXT = "test.txt";
    protected static final String TEST_FILE_NAME_TEST_PDF = "test.pdf";
    protected static final byte[] TEST_DATA_TXT_CONTENT = new byte[200];

    protected static final byte[] TEST_DATA_PDF_CONTENT = new byte[256];
    protected static final String ANNOTATION_DOMAIN_COM_GUARDTIME = "com.guardtime";

    protected static final String ANNOTATION_CONTENT = "42";
    protected static final String DOCUMENTS_MANIFEST_URI = "/META-INF/datamanifest-1.tlv";

    protected static final String ANNOTATIONS_MANIFEST_URI = "/META-INF/annotmanifest-1.tlv";

    protected final DefaultRuleStateProvider defaultRuleStateProvider = new DefaultRuleStateProvider();

    protected Annotation STRING_ENVELOPE_ANNOTATION;
    protected Document TEST_DOCUMENT_HELLO_TEXT;
    protected Document TEST_DOCUMENT_HELLO_PDF;
    protected final List<AutoCloseable> envelopeElements = new LinkedList<>();

    @Before
    public void setUpDocumentsAndAnnotations() {
        STRING_ENVELOPE_ANNOTATION = new StringAnnotation(EnvelopeAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME);
        TEST_DOCUMENT_HELLO_TEXT = new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        TEST_DOCUMENT_HELLO_PDF = new StreamDocument(new ByteArrayInputStream(TEST_DATA_PDF_CONTENT), MIME_TYPE_APPLICATION_PDF, TEST_FILE_NAME_TEST_PDF);
        envelopeElements.addAll(Arrays.asList(
                TEST_DOCUMENT_HELLO_PDF,
                TEST_DOCUMENT_HELLO_TEXT,
                STRING_ENVELOPE_ANNOTATION));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected SignatureFactory mockedSignatureFactory;

    @Mock
    protected EnvelopeManifestFactory mockedManifestFactory;

    @Mock
    protected ManifestFactoryType mockedManifestFactoryType;

    @Mock
    protected SignatureFactoryType mockedSignatureFactoryType;

    @Mock
    protected DocumentsManifest mockedDocumentsManifest;

    @Mock
    protected AnnotationsManifest mockedAnnotationsManifest;

    @Mock
    protected SingleAnnotationManifest mockedSingleAnnotationManifest;

    @Mock
    protected Manifest mockedManifest;

    @Mock
    protected HashAlgorithmProvider mockHashAlgorithmProvider;

    @Mock
    protected IndexProviderFactory mockIndexProviderFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockedManifestFactory.getManifestFactoryType()).thenReturn(mockedManifestFactoryType);
        when(mockedManifestFactory.getHashAlgorithmProvider()).thenReturn(mockHashAlgorithmProvider);
        when(mockedManifestFactory.createDocumentsManifest(anyListOf(Document.class), anyString())).thenReturn(mockedDocumentsManifest);
        when(mockedManifestFactory.createAnnotationsManifest(anyMap(), anyString())).thenReturn(mockedAnnotationsManifest);
        when(mockedManifestFactory.createSingleAnnotationManifest(Mockito.any(DocumentsManifest.class), Mockito.any(Annotation.class), anyString())).thenReturn(mockedSingleAnnotationManifest);
        when(mockedManifestFactory.createManifest(Mockito.any(DocumentsManifest.class), Mockito.any(AnnotationsManifest.class), Mockito.any(SignatureFactoryType.class), anyString(), anyString())).thenReturn(mockedManifest);
        when(mockedSignatureFactory.getSignatureFactoryType()).thenReturn(mockedSignatureFactoryType);
        when(mockedSignatureFactoryType.getSignatureMimeType()).thenReturn(SIGNATURE_MIME_TYPE);
    }

    @After
    public void tearDown() throws Exception {
        closeAll(envelopeElements);
    }

    protected void closeAll(Collection<? extends AutoCloseable> list) throws Exception {
        for (AutoCloseable c : list) {
            c.close();
        }
    }

    protected File loadFile(String filePath) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        return new File(url.toURI());
    }
}
