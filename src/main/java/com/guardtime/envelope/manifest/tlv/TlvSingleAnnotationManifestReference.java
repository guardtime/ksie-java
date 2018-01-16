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

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

class TlvSingleAnnotationManifestReference extends TlvFileReference {

    public static final int ANNOTATION_INFO_REFERENCE = 0xb04;

    TlvSingleAnnotationManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    TlvSingleAnnotationManifestReference(Annotation annotation, SingleAnnotationManifest singleAnnotationManifest,
                                                HashAlgorithmProvider algorithmProvider)
            throws TLVParserException, DataHashException {
        super(
                singleAnnotationManifest.getPath(),
                generateHashes(singleAnnotationManifest, algorithmProvider.getFileReferenceHashAlgorithms()),
                annotation.getAnnotationType().getContent()
        );
    }

    @Override
    public int getElementType() {
        return ANNOTATION_INFO_REFERENCE;
    }

}
