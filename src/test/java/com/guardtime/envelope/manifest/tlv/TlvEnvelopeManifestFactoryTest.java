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

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.guardtime.envelope.util.Util.hash;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class TlvEnvelopeManifestFactoryTest extends AbstractTlvManifestTest {

    private TlvEnvelopeManifestFactory factory = new TlvEnvelopeManifestFactory();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedDocumentsManifest.getPath()).thenReturn(TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateTlvEnvelopeManifestFactoryWithoutHashAlgorithm_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm provider");
        new TlvEnvelopeManifestFactory(null);
    }

    @Test
    public void testCreateSingleAnnotationManifestWithoutDocumentsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Documents manifest");
        factory.createSingleAnnotationManifest(null, mockAnnotation, "singleAnnotationManifestName");
    }

    @Test
    public void testCreateSingleAnnotationManifestWithoutAnnotation_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotation");
        factory.createSingleAnnotationManifest(mockedDocumentsManifest, null, "singleAnnotationManifestName");
    }

    @Test
    public void testCreateSingleAnnotationManifest() throws Exception {
        DataHash dataHashForEmptyData = hash(EMPTY_INPUT_STREAM, HashAlgorithm.SHA2_256);
        doReturn(dataHashForEmptyData).when(mockedDocumentsManifest).getDataHash(any(HashAlgorithm.class));
        TlvSingleAnnotationManifest singleAnnotationManifest =
                factory.createSingleAnnotationManifest(mockedDocumentsManifest, mockAnnotation, "singleAnnotationManifestName");
        assertNotNull(singleAnnotationManifest);
        assertNotNull(singleAnnotationManifest.getAnnotationReference());
        assertNotNull(singleAnnotationManifest.getDocumentsManifestReference());
    }

    @Test
    public void testCreateAnnotationsManifestWithoutSingleAnnotationManifestsOK() throws Exception {
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(
                new HashMap<Annotation, TlvSingleAnnotationManifest>(),
                "annotationsManifestName"
        );
        assertNotNull(manifest);
    }

    @Test
    public void testCreateAnnotationsManifestOK() throws Exception {
        Map<Annotation, TlvSingleAnnotationManifest> annotationManifests = new HashMap();
        annotationManifests.put(mockAnnotation, mockSingleAnnotationManifest);
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(annotationManifests, "annotationsManifestName");

        assertNotNull("Manifest was not created", manifest);
    }

    @Test
    public void testCreateDocumentsManifestWithEmptyDocumentsList_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files list must not be empty");
        factory.createDocumentsManifest(new ArrayList<Document>(), "documentsManifestName");
    }

    @Test
    public void testCreateDocumentsManifestWithoutDocumentsList_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document files list must be present");
        factory.createDocumentsManifest(null, "documentsManifestName");
    }

    @Test
    public void testCreateDocumentsManifestOK() throws Exception {
        TlvDocumentsManifest documentsManifest = factory.createDocumentsManifest(
                Collections.singletonList(testDocumentHelloText),
                "documentsManifestName"
        );
        assertNotNull("Manifest was not created", documentsManifest);
        assertEquals(1, documentsManifest.getDocumentReferences().size());
    }

    @Test
    public void testCreateManifestWithoutDocumentsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Documents manifest must be present");
        factory.createManifest(null, mockAnnotationsManifest, mockedSignatureFactoryType, "signatureName", "manifestName");
    }

    @Test
    public void testCreateManifestWithoutAnnotationsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotations manifest must be present");
        factory.createManifest(mockDocumentsManifest, null, mockedSignatureFactoryType, "signatureName", "manifestName");
    }

    @Test
    public void testCreateManifestOK() throws Exception {
        TlvManifest manifest = factory.createManifest(
                mockDocumentsManifest,
                mockAnnotationsManifest,
                mockedSignatureFactoryType,
                "signatureName",
                "manifestName");

        assertNotNull("Manifest was not created", manifest);
    }

    @Test
    public void testGetManifestFactoryType() {
        TlvManifestFactoryType type = factory.getManifestFactoryType();
        assertNotNull(type);
        assertNotNull(type.getManifestFileExtension());
        assertNotNull(type.getName());
    }

}