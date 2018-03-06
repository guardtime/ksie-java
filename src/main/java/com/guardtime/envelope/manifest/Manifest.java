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

package com.guardtime.envelope.manifest;

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.signature.EnvelopeSignature;

import java.io.IOException;
import java.io.InputStream;

/**
 * Envelope structure manifest containing references to the {@link AnnotationsManifest}, {@link DocumentsManifest}, and
 * {@link EnvelopeSignature} contained in the envelope. This is the root manifest of the
 * envelope structure.
 */
public interface Manifest extends EnvelopeElement {

    /**
     * @return InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

    FileReference getDocumentsManifestReference();

    FileReference getAnnotationsManifestReference();

    SignatureReference getSignatureReference();

    ManifestFactoryType getManifestFactoryType();
}
