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

package com.guardtime.envelope.annotation;

import com.guardtime.envelope.EnvelopeElement;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents annotations that can be used in envelope. Combines annotation data and annotation
 * meta-data into one object.
 */
public interface Annotation extends AutoCloseable, EnvelopeElement {

    EnvelopeAnnotationType getAnnotationType();

    String getDomain();

    /**
     * Returns {@link InputStream} containing the annotation data.
     * @throws IOException when there is a problem creating or accessing the InputStream.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Sets the path value used by {@link EnvelopeElement#getPath()}.
     */
    void setPath(String path);

}
