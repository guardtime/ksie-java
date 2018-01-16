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

import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

class TlvAnnotationsManifestReference extends TlvFileReference {

    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;
    private static final String ANNOTATIONS_MANIFEST_TYPE = "ksie10/annotmanifest";

    TlvAnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    TlvAnnotationsManifestReference(AnnotationsManifest annotationsManifest, HashAlgorithmProvider algorithmProvider)
            throws TLVParserException, DataHashException {
        super(
                annotationsManifest.getPath(),
                generateHashes(annotationsManifest, algorithmProvider.getFileReferenceHashAlgorithms()),
                ANNOTATIONS_MANIFEST_TYPE
        );
    }

    @Override
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }

}
