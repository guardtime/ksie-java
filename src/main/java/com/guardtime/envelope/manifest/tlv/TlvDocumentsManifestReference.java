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

package com.guardtime.envelope.manifest.tlv;

import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

class TlvDocumentsManifestReference extends TlvFileReference {

    public static final int DOCUMENTS_MANIFEST_REFERENCE = 0xb01;
    private static final String DATA_FILES_MANIFEST = "ksie10/datamanifest";

    TlvDocumentsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    TlvDocumentsManifestReference(DocumentsManifest documentsManifest, HashAlgorithmProvider algorithmProvider)
            throws TLVParserException, DataHashException {
        super(
                documentsManifest.getPath(),
                generateHashes(documentsManifest, algorithmProvider.getFileReferenceHashAlgorithms()),
                DATA_FILES_MANIFEST
        );
    }

    @Override
    public int getElementType() {
        return DOCUMENTS_MANIFEST_REFERENCE;
    }

}
