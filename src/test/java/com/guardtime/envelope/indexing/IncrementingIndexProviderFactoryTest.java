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

package com.guardtime.envelope.indexing;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.guardtime.envelope.packaging.EntryNameProvider.META_INF;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IncrementingIndexProviderFactoryTest extends AbstractEnvelopeTest {

    private IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();

    @Test
    public void testCreateWithValidEnvelope() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .build();
        try (Envelope envelope = packagingFactory.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
            IndexProvider indexProvider = indexProviderFactory.create(envelope);
            Assert.assertEquals("2", indexProvider.getNextSignatureIndex());
        }
    }

    @Test
    public void testCreateWithMixedEnvelope() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        try (Envelope envelope = packagingFactory.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
            IndexProvider indexProvider = indexProviderFactory.create(envelope);
            Assert.assertEquals("1", indexProvider.getNextSignatureIndex());
        }
    }

    @Test
    public void testValuesIncrement() {
        IndexProvider indexProvider = indexProviderFactory.create();
        int firstIndex = Integer.parseInt(indexProvider.getNextAnnotationIndex());
        int secondIndex = Integer.parseInt(indexProvider.getNextAnnotationIndex());
        assertTrue(firstIndex < secondIndex);
    }

    @Test
    public void testDifferentManifestIndexesStartFromSameValue() {
        IndexProvider indexProvider = indexProviderFactory.create();
        int manifestIndex = Integer.parseInt(indexProvider.getNextManifestIndex());
        int documentManifestIndex = Integer.parseInt(indexProvider.getNextDocumentsManifestIndex());
        assertEquals(manifestIndex, documentManifestIndex);
    }

    @Test
    public void testWithClashingUnkownFiles() {
        Envelope mockEnvelope = mock(Envelope.class);
        List<UnknownDocument> unknownFileList = new ArrayList<>();
        unknownFileList.add(new ParsedDocument(mock(ParsingStore.class), "k", "m", META_INF + "/manifest-8.tlv"));
        when(mockEnvelope.getUnknownFiles()).thenReturn(unknownFileList);
        IndexProvider indexProvider = indexProviderFactory.create(mockEnvelope);
        Assert.assertEquals("9", indexProvider.getNextSignatureIndex());
    }

}
