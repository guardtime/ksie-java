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

package com.guardtime.envelope.signature.ksi;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class KsiSignatureFactoryTest extends AbstractEnvelopeTest {

    @Mock
    private KSI mockKsi;

    @Mock
    private KSISignature mockSignature;

    @Before
    public void setUpMockKsi() throws KSIException {
        when(mockKsi.sign(Mockito.any(DataHash.class))).thenReturn(mockSignature);
        when(mockKsi.read(Mockito.any(byte[].class))).thenReturn(mockSignature);
    }

    @Test
    public void testCreateFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI must be present");
        new KsiSignatureFactory(null);
    }

    @Test
    public void testCreateFactory_OK() throws Exception {
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        assertNotNull(signatureFactory);
    }

    @Test
    public void testCreate() throws Exception {
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        DataHash testHash = new DataHash(
                HashAlgorithm.SHA2_256,
                "TestStringTestingStuffLongString".getBytes(StandardCharsets.UTF_8)
        );
        EnvelopeSignature signature = signatureFactory.create(testHash);
        assertNotNull(signature);
    }

    @Test
    public void testRead() throws Exception {
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        EnvelopeSignature signature = signatureFactory.read(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        assertNotNull(signature);
    }
}