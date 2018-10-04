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

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Universal builder for {@link Document}
 */
public class DocumentFactory {
    private final ParsingStore parsingStore;

    public DocumentFactory(ParsingStore store) {
        Util.notNull(store, "Parsing store");
        this.parsingStore = store;
    }

    public Document create(File file, String mimetype) {
        return new FileDocument(file, mimetype);
    }

    public Document create(File file, String mimetype, String filename) {
        return new FileDocument(file, mimetype, filename);
    }

    public Document create(Collection<DataHash> hashList, String mimetype, String filename) {
        return new EmptyDocument(filename, mimetype, new ArrayList<>(hashList));
    }

    public Document create(EnvelopeElement element) {
        return new InternalDocument(element);
    }

    public Document create(ParsingStoreReference reference, String mimetype, String filename) {
        return new ParsedDocument(reference, mimetype, filename);
    }

    /**
     * NB! Does not close the stream! Just reads from it.
     */
    public Document create(InputStream stream, String mimetype, String filename) {
        return new ParsedDocument(addToStore(stream, filename), mimetype, filename);
    }

    public Document create(Document original) {
        if (original instanceof FileDocument) {
            return create(((FileDocument) original).file, original.getMimeType(), original.getFileName());
        } else if (original instanceof EmptyDocument) {
            return create(((EmptyDocument) original).dataHashMap.values(), original.getMimeType(), original.getFileName());
        } else if (original instanceof InternalDocument) {
            return create(((InternalDocument) original).element);
        } else {
            try (InputStream inputStream = original.getInputStream()) {
                return create(inputStream, original.getMimeType(), original.getFileName());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to access content of Document!", e);
            }
        }
    }

    private ParsingStoreReference addToStore(InputStream data, String path) {
        notNull(data, "Input stream");
        try {
            return parsingStore.store(data, path);
        } catch (ParsingStoreException e) {
            throw new IllegalArgumentException("Can not copy input stream to parsing store!", e);
        }
    }

}
