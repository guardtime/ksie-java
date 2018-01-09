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

package com.guardtime.envelope.manifest.tlv;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.hash.SingleHashAlgorithmProvider;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;
import com.guardtime.ksi.util.Util;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.guardtime.envelope.util.Util.hash;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class AbstractTlvManifestTest extends AbstractEnvelopeTest {

    protected static final ByteArrayInputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);
    protected static final int TLV_ELEMENT_URI_TYPE = 0x01;
    protected static final int TLV_ELEMENT_DATA_HASH_TYPE = 0x02;
    protected static final int TLV_ELEMENT_MIME_TYPE = 0x03;
    protected static final int TLV_ELEMENT_DOMAIN_TYPE = 0x04;
    protected static final int DOCUMENTS_MANIFEST_REFERENCE_TYPE = 0xb01;
    protected static final int ANNOTATIONS_MANIFEST_REFERENCE_TYPE = 0xb02;
    protected static final int DOCUMENT_REFERENCE_TYPE = 0xb03;
    protected static final int ANNOTATION_INFO_REFERENCE_TYPE = 0xb04;
    protected static final int ANNOTATION_REFERENCE_TYPE = 0xb05;
    protected static final int SIGNATURE_REFERENCE_TYPE = 0xb06;

    protected static final byte[] SINGLE_ANNOTATION_MANIFEST_MAGIC = "KSIEANNT".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] ANNOTATIONS_MANIFEST_MAGIC = "KSIEANMF".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] DOCUMENTS_MANIFEST_MAGIC = "KSIEDAMF".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] SIGNATURE_MANIFEST_MAGIC = "KSIEMFST".getBytes(StandardCharsets.UTF_8);

    protected static final String ANNOTATIONS_MANIFEST_TYPE = "ksie10/annotmanifest";
    protected static final String DOCUMENTS_MANIFEST_TYPE = "ksie10/datamanifest";
    protected static final String SIGNATURE_TYPE = "application/ksi-signature";
    protected static final String MOCK_URI = "/mock/mock";
    protected static final String SIGNATURE_URI = "/META-INF/signature4.ksig";
    protected static final String SINGLE_ANNOTATION_MANIFEST_URI = "/META-INF/annotation1.tlv";
    protected static final HashAlgorithmProvider DEFAULT_HASH_ALGORITHM_PROVIDER = new SingleHashAlgorithmProvider(HashAlgorithm.SHA2_256);

    @Mock
    protected TlvDocumentsManifest mockDocumentsManifest;

    @Mock
    protected TlvAnnotationsManifest mockAnnotationsManifest;

    @Mock
    protected Annotation mockAnnotation;

    @Mock
    protected TlvSingleAnnotationManifest mockSingleAnnotationManifest;

    @Mock
    protected Document mockDocument;
    protected DataHash dataHash;
    @Mock
    private AnnotationDataReference mockAnnotationDataReference;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.dataHash = hash(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), HashAlgorithm.SHA2_256);
        DataHash dataHashForEmptyData = hash(EMPTY_INPUT_STREAM, HashAlgorithm.SHA2_256);
        when(mockDocumentsManifest.getInputStream()).thenReturn(EMPTY_INPUT_STREAM);
        when(mockDocumentsManifest.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHashForEmptyData);
        when(mockAnnotationsManifest.getInputStream()).thenReturn(EMPTY_INPUT_STREAM);
        when(mockAnnotationsManifest.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHashForEmptyData);
        when(mockAnnotationsManifest.getPath()).thenReturn(MOCK_URI);

        when(mockAnnotation.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockAnnotation.getAnnotationType()).thenReturn(EnvelopeAnnotationType.NON_REMOVABLE);
        when(mockAnnotation.getDomain()).thenReturn(ANNOTATION_DOMAIN_COM_GUARDTIME);
        when(mockAnnotation.getPath()).thenReturn(MOCK_URI);

        when(mockSingleAnnotationManifest.getInputStream()).thenReturn(EMPTY_INPUT_STREAM);
        when(mockSingleAnnotationManifest.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHashForEmptyData);
        when(mockSingleAnnotationManifest.getAnnotationReference()).thenReturn(mockAnnotationDataReference);
        when(mockSingleAnnotationManifest.getPath()).thenReturn(MOCK_URI);

        when(mockAnnotationDataReference.getDomain()).thenReturn(ANNOTATION_DOMAIN_COM_GUARDTIME);
        when(mockAnnotationDataReference.getUri()).thenReturn(MOCK_URI);
        when(mockAnnotationDataReference.getHash()).thenReturn(dataHash);

        when(mockDocument.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(dataHash);
        when(mockDocument.getFileName()).thenReturn(TEST_FILE_NAME_TEST_TXT);
        when(mockDocument.getMimeType()).thenReturn(MIME_TYPE_APPLICATION_TXT);
    }

    protected void testMagic(InputStream stream, byte[] magic) throws Exception {
        byte[] data = new byte[magic.length];
        stream.read(data);
        assertArrayEquals("Magic doesn't match", magic, data);
    }

    protected TLVElement createReference(int referenceType, String referenceUri, String referenceMime, DataHash dataHash) throws Exception {
        return createReference(referenceType, referenceUri, referenceMime, Collections.singletonList(dataHash));
    }

    protected TLVElement createReference(int referenceType, String referenceUri, String referenceMime, List<DataHash> dataHashList) throws Exception {
        TLVElement reference = new TLVElement(false, false, referenceType);
        if (referenceUri != null) {
            TLVElement uri = new TLVElement(false, false, TLV_ELEMENT_URI_TYPE);
            uri.setStringContent(referenceUri);
            reference.addChildElement(uri);
        }
        if (referenceMime != null) {
            TLVElement mime = new TLVElement(false, false, TLV_ELEMENT_MIME_TYPE);
            mime.setStringContent(referenceMime);
            reference.addChildElement(mime);
        }
        if (dataHashList != null) {
            for (DataHash dataHash : dataHashList) {
                TLVElement hash = new TLVElement(false, false, TLV_ELEMENT_DATA_HASH_TYPE);
                hash.setDataHashContent(dataHash);
                reference.addChildElement(hash);
            }
        }
        return reference;
    }

    protected TLVElement createAnnotationReferenceElement() throws Exception {
        TLVElement element = createReference(ANNOTATION_REFERENCE_TYPE, MOCK_URI, null, dataHash);
        TLVElement domainElement = new TLVElement(false, false, TLV_ELEMENT_DOMAIN_TYPE);
        domainElement.setStringContent(ANNOTATION_DOMAIN_COM_GUARDTIME);
        element.addChildElement(domainElement);
        return element;
    }

    protected byte[] join(byte[]... arrays) {
        byte[] out = new byte[0];
        for (byte[] a : arrays) {
            out = Util.join(out, a);
        }
        return out;
    }

    protected String getUri(TLVStructure structure) throws TLVParserException {
        return getFirstChildElement(structure, TLV_ELEMENT_URI_TYPE).getDecodedString();
    }

    protected DataHash getDataHash(TLVStructure reference) throws TLVParserException {
        return getFirstChildElement(reference, TLV_ELEMENT_DATA_HASH_TYPE).getDecodedDataHash();
    }

    protected String getMimeType(TLVStructure reference) throws TLVParserException {
        return getFirstChildElement(reference, TLV_ELEMENT_MIME_TYPE).getDecodedString();
    }

    protected String getDomain(TlvAnnotationDataReference reference) throws TLVParserException {
        return getFirstChildElement(reference, TLV_ELEMENT_DOMAIN_TYPE).getDecodedString();
    }

    protected TLVElement getFirstChildElement(TLVStructure structure, int tag) {
        return structure.getRootElement().getFirstChildElement(tag);
    }

}
