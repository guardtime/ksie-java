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

package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.DocumentFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZipEnvelopeWriterTest extends AbstractEnvelopeTest {

    @Test
    public void testAddDocumentWithDirectoryName_ThrowsIOException() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage(" is an invalid document file name!");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mock(EnvelopeSignature.class));
        Document testDocument = DocumentFactory.create(new ByteArrayInputStream(new byte[0]), "some type", "folder/");
        try (Envelope envelope = packagingFactory.create(
                singletonList(testDocument),
                singletonList(stringEnvelopeAnnotation)
        )) {
            ZipEnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
        }
    }

}
