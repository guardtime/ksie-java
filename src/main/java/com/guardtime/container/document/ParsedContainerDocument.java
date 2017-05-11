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

package com.guardtime.container.document;

import com.guardtime.container.packaging.parsing.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;

/**
 * Represents a {@link ContainerDocument} that has been parsed in. Uses a {@link ParsingStore} from where to access the data of
 * the {@link ContainerDocument}
 */
public class ParsedContainerDocument extends AbstractContainerDocument implements UnknownDocument {

    private final ParsingStore parsingStore;
    private final String key;

    public ParsedContainerDocument(ParsingStore store, String key, String mimeType, String fileName) {
        super(mimeType, fileName);
        notNull(store, "Parsing store");
        notNull(key, "Parsing store key");
        this.parsingStore = store;
        this.key = key;
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
    public void close() throws Exception {
        parsingStore.remove(key);
    }
}
