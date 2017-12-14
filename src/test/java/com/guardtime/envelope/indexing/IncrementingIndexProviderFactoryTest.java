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
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IncrementingIndexProviderFactoryTest extends AbstractEnvelopeTest {

    private IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();

    @Test
    public void testCreateWithValidEnvelope() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .build();
        try (Envelope envelope = packagingFactory.create(
                singletonList(TEST_DOCUMENT_HELLO_TEXT),
                singletonList(STRING_ENVELOPE_ANNOTATION)
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
                singletonList(TEST_DOCUMENT_HELLO_TEXT),
                singletonList(STRING_ENVELOPE_ANNOTATION)
        )) {
            IndexProvider indexProvider = indexProviderFactory.create(envelope);
            Assert.assertEquals("1", indexProvider.getNextSignatureIndex());
        }
    }

    @Test
    public void testValuesIncrement() throws Exception {
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

}