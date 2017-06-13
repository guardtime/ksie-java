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

package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SingleHashAlgorithmProviderTest {

    public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA2_256;
    private SingleHashAlgorithmProvider singleHashAlgorithmProvider = new SingleHashAlgorithmProvider(HASH_ALGORITHM);

    @Test
    public void testGetFileReferenceHashAlgorithms() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getFileReferenceHashAlgorithms().get(0));
    }

    @Test
    public void testGetDocumentReferenceHashAlgorithms() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getDocumentReferenceHashAlgorithms().get(0));
    }

    @Test
    public void testGetAnnotationDataReferenceHashAlgorithm() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getAnnotationDataReferenceHashAlgorithm());
    }

    @Test
    public void testGetSigningHashAlgorithm() throws Exception {
        assertEquals(HASH_ALGORITHM, singleHashAlgorithmProvider.getSigningHashAlgorithm());
    }

}