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
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Represents a {@link Document} that has been parsed in. Uses a {@link ParsingStore} from where to access the data of
 * the {@link Document}
 */
class ParsedDocument extends AbstractDocument implements UnknownDocument {

    private final ParsingStoreReference parsingStoreReference;

    /**
     * Creates {@link Document} with provided MIME-type and file name. The data for the {@link Document} is accessible by the
     * provided {@link ParsingStoreReference} with the provided key.
     * @param reference         The {@link ParsingStoreReference} that provides the document data.
     * @param mimeType          The MIME-type of the {@link Document}.
     * @param fileName          The file name to be used for the {@link Document}.
     */
    protected ParsedDocument(ParsingStoreReference reference, String mimeType, String fileName) {
        super(mimeType, fileName);
        notNull(reference, "Parsing store reference");
        this.parsingStoreReference = reference;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = parsingStoreReference.get();
        if (inputStream == null) {
            throw new IOException(
                    "Failed to acquire input stream from parsing store for key '" + parsingStoreReference.getUuid() + "'"
            );
        }
        return inputStream;
    }

    @Override
    public void close() {
        if (!closed) {
            parsingStoreReference.unstore();
            super.close();
        }
    }
}
