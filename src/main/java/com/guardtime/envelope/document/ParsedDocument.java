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

package com.guardtime.envelope.document;

import com.guardtime.envelope.packaging.parsing.store.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Represents a {@link Document} that has been parsed in. Uses a {@link ParsingStore} from where to access the data of
 * the {@link Document}
 */
public class ParsedDocument extends AbstractDocument implements UnknownDocument {

    private final ParsingStore parsingStore;
    private final String key;

    /**
     *
     * Creates {@link Document} with provided MIME-type and file name. The data for the {@link Document} is contained in the
     * provided {@link ParsingStore} and can be accessed with the provided key.
     * @param store             The {@link ParsingStore} that contains the document data.
     * @param parsingStoreKey   The key for the data in {@link ParsingStore} of the document.
     * @param mimeType          The MIME-type of the {@link Document}.
     * @param fileName          The file name to be used for the {@link Document}.
     */
    public ParsedDocument(ParsingStore store, String parsingStoreKey, String mimeType, String fileName) {
        super(mimeType, fileName);
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
