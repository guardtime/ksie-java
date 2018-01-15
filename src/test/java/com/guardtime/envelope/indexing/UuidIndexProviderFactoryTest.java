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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotEquals;

public class UuidIndexProviderFactoryTest extends AbstractEnvelopeTest {

    private IndexProviderFactory indexProviderFactory = new UuidIndexProviderFactory();

    @Test
    public void testCreateWithMixedEnvelope() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .build();
        try (Envelope envelope = packagingFactory.create(
                singletonList(testDocumentHelloText),
                singletonList(stringEnvelopeAnnotation)
        )) {
            IndexProvider indexProvider = indexProviderFactory.create(envelope);
            Assert.assertNotNull(indexProvider);
        }
    }

    @Test
    public void testNextValueDiffersFromPrevious() {
        IndexProvider indexProvider = indexProviderFactory.create();
        assertNotEquals(indexProvider.getNextAnnotationIndex(), indexProvider.getNextAnnotationIndex());
    }

    @Test
    public void testDifferentManifestIndexesDoNotMatch() {
        IndexProvider indexProvider = indexProviderFactory.create();
        assertNotEquals(indexProvider.getNextManifestIndex(), indexProvider.getNextDocumentsManifestIndex());
    }

}
