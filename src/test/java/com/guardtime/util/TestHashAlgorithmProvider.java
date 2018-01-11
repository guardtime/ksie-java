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

package com.guardtime.util;

import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.Collections;
import java.util.List;

public class TestHashAlgorithmProvider implements HashAlgorithmProvider {
    private List<HashAlgorithm> fileReferenceHashAlgorithms;
    private List<HashAlgorithm> documentReferenceHashAlgorithms;
    private HashAlgorithm annotationDataReferenceHashAlgorithm;
    private HashAlgorithm signingHashAlgorithm;

    public TestHashAlgorithmProvider() {
        this.fileReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.SHA2_256);
        this.documentReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.SHA2_256);
        this.annotationDataReferenceHashAlgorithm = HashAlgorithm.SHA2_256;
        this.signingHashAlgorithm = HashAlgorithm.SHA2_256;
    }

    public TestHashAlgorithmProvider(HashAlgorithm algorithm) {
        this.fileReferenceHashAlgorithms = Collections.singletonList(algorithm);
        this.documentReferenceHashAlgorithms = Collections.singletonList(algorithm);
        this.annotationDataReferenceHashAlgorithm = algorithm;
        this.signingHashAlgorithm = algorithm;
    }

    public TestHashAlgorithmProvider(List<HashAlgorithm> fileReferenceHashAlgorithms,
                                     List<HashAlgorithm> documentReferenceHashAlgorithms,
                                     HashAlgorithm annotationDataReferenceHashAlgorithm,
                                     HashAlgorithm signingHashAlgorithm) {
        this.fileReferenceHashAlgorithms = fileReferenceHashAlgorithms;
        this.documentReferenceHashAlgorithms = documentReferenceHashAlgorithms;
        this.annotationDataReferenceHashAlgorithm = annotationDataReferenceHashAlgorithm;
        this.signingHashAlgorithm = signingHashAlgorithm;

    }

    @Override
    public List<HashAlgorithm> getFileReferenceHashAlgorithms() {
        return fileReferenceHashAlgorithms;
    }

    @Override
    public List<HashAlgorithm> getDocumentReferenceHashAlgorithms() {
        return documentReferenceHashAlgorithms;
    }

    @Override
    public HashAlgorithm getAnnotationDataReferenceHashAlgorithm() {
        return annotationDataReferenceHashAlgorithm;
    }

    @Override
    public HashAlgorithm getSigningHashAlgorithm() {
        return signingHashAlgorithm;
    }
}
