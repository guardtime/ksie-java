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

import com.guardtime.envelope.packaging.parsing.store.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Represents an {@link Annotation} that has been parsed in. Uses a {@link ParsingStore} from where to access the data of
 * the {@link Annotation}.
 */
public class ParsedAnnotation extends AbstractAnnotation {

    private final ParsingStore parsingStore;
    private final String key;

    /**
     * Creates {@link Annotation} with provided type and domain. The annotation value is provided via {@link ParsingStore} and
     * the key of the data in the {@link ParsingStore}.
     * @param store             The {@link ParsingStore} that contains the annotation data.
     * @param parsingStoreKey   The key for the data in {@link ParsingStore} of the annotation.
     * @param domain            The key of the annotation key-value pair. To prevent key conflicts, the prefix x.y.z. is
     *                          reserved to the entity controlling the Internet domain name z.y.x.
     * @param type              The annotation type, indicating the persistence of the annotation, see
     *                          {@link EnvelopeAnnotationType} for details.
     */
    public ParsedAnnotation(ParsingStore store, String parsingStoreKey, String domain, EnvelopeAnnotationType type) {
        super(domain, type);
        notNull(store, "Parsing store");
        notNull(parsingStoreKey, "Parsing store key");
        this.parsingStore = store;
        this.key = parsingStoreKey;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = parsingStore.get(key);
        if (inputStream == null) {
            throw new IOException("Failed to acquire input stream from parsing store for key '" + key + "'");
        }
        return inputStream;
    }

    @Override
    public void close() {
        parsingStore.remove(key);
    }
}
