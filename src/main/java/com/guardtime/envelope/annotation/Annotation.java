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

package com.guardtime.envelope.annotation;

import com.guardtime.envelope.EnvelopeElement;

import java.io.IOException;
import java.io.InputStream;
s
/**
 * Represents annotations that can be used in the envelope. Combines annotation data and annotation
 * meta-data into one object.
 * <p>
 * Annotation can be any <code>key:value</code> pair that the user finds to be reasonable to describe the signed data.
 * E.g. information about the author and/or contents of the document to be signed.
 * </p><p>
 * To prevent key conflicts, it is suggested to use the prefix <code>x.y.z.</code>,
 * where <code>z.y.x</code> is the Internet domain controlled by the entity owning the implementation or data.
 * </p><p>
 * While creating the annotation, the {@link EnvelopeAnnotationType} should be chosen wisely according to the user's needs:
 * removable, partly removable, non-removable.
 * </p>
 */
public interface Annotation extends AutoCloseable, EnvelopeElement {

    /**
     * @return Type of annotation as represented by {@link EnvelopeAnnotationType} ENUM.
     */
    EnvelopeAnnotationType getAnnotationType();

    /**
     * @return Key of the annotation key-value pair.
     */
    String getDomain();

    /**
     * @return {@link InputStream} containing the <code>value</code> of the
     * <code>key:value</code> pair of the annotation.
     * @throws IOException when there is a problem creating or accessing the {@link InputStream}.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Sets the path value used by {@link EnvelopeElement#getPath()}.
     * Used internally by {@link com.guardtime.envelope.packaging.EnvelopePackagingFactory}.
     * @param path Path of the annotation data within an {@link com.guardtime.envelope.packaging.Envelope}
     */
    void setPath(String path);

}
