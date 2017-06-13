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

package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Container internal structure manifest that contains some meta-data and reference to annotation data.
 */
public interface SingleAnnotationManifest extends MultiHashElement {

    /**
     * Returns a reference pointing to annotation data in the container.
     */
    AnnotationDataReference getAnnotationReference();

    FileReference getDocumentsManifestReference();

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

}
