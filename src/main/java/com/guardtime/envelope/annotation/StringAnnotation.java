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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Annotation that is based on a String as the data source.
 */
public class StringAnnotation extends AbstractAnnotation {

    private final String content;

    /**
     * Creates {@link Annotation} with provided type, domain and given {@link String} as value.
     * @param content The {@link String} content of the {@link Annotation}.
     * @param domain The key of the annotation key-value pair. To prevent key conflicts, the prefix x.y.z. is reserved to the
     *               entity controlling the Internet domain name z.y.x.
     * @param type annotation type, indicating the persistence of the annotation, see {@link EnvelopeAnnotationType} for details.
     */
    public StringAnnotation(String content, String domain, EnvelopeAnnotationType type) {
        super(domain, type);
        notNull(content, "Content");
        this.content = content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
