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

import java.util.Collections;
import java.util.List;

/**
 * Simple {@link HashAlgorithmProvider} that provides the same {@link HashAlgorithm} for every output.
 */
public class SingleHashAlgorithmProvider implements HashAlgorithmProvider {
    private final HashAlgorithm algorithm;

    /**
     * @param hashAlgorithm    The {@link HashAlgorithm} to be used as output by the created instance.
     */
    public SingleHashAlgorithmProvider(HashAlgorithm hashAlgorithm) {
        if (!hashAlgorithm.getStatus().equals(HashAlgorithm.Status.NORMAL)) {
            throw new IllegalArgumentException("Invalid HashAlgorithm provided! Only accept with status 'NORMAL', not '" + hashAlgorithm.getStatus() + "'");
        }
        this.algorithm = hashAlgorithm;
    }

    @Override
    public List<HashAlgorithm> getFileReferenceHashAlgorithms() {
        return Collections.singletonList(algorithm);
    }

    @Override
    public List<HashAlgorithm> getDocumentReferenceHashAlgorithms() {
        return Collections.singletonList(algorithm);
    }

    @Override
    public HashAlgorithm getAnnotationDataReferenceHashAlgorithm() {
        return algorithm;
    }

    @Override
    public HashAlgorithm getSigningHashAlgorithm() {
        return algorithm;
    }
}
