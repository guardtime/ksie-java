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

package com.guardtime.envelope.hash;

import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.List;

/**
 * Helper that contains a selection of {@link HashAlgorithm}s that are to be used for producing {@link
 * com.guardtime.ksi.hashing.DataHash}es.
 */
public interface HashAlgorithmProvider {

    /**
     * Returns a {@link List} of all {@link HashAlgorithm}s to be used for creating {@link
     * com.guardtime.ksi.hashing.DataHash}es for {@link com.guardtime.envelope.manifest.FileReference}.
     */
    List<HashAlgorithm> getFileReferenceHashAlgorithms();

    /**
     * Returns a {@link List} of all {@link HashAlgorithm}s to be used for creating {@link
     * com.guardtime.ksi.hashing.DataHash}es for {@link com.guardtime.envelope.manifest.FileReference} used
     * specifically for {@link EnvelopeDocument}s.
     */
    List<HashAlgorithm> getDocumentReferenceHashAlgorithms();

    /**
     * Returns a {@link HashAlgorithm} to be used for creating {@link
     * com.guardtime.ksi.hashing.DataHash} for {@link com.guardtime.envelope.manifest.AnnotationDataReference}.
     */
    HashAlgorithm getAnnotationDataReferenceHashAlgorithm();

    /**
     * Returns a {@link HashAlgorithm} to be used when creating signature for the envelope.
     */
    HashAlgorithm getSigningHashAlgorithm();
}
