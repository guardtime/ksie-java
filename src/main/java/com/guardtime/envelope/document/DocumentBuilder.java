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
import com.guardtime.envelope.packaging.parsing.store.ActiveParsingStoreProvider;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;
import com.guardtime.ksi.hashing.DataHash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.guardtime.envelope.util.Util.notEmpty;
import static com.guardtime.envelope.util.Util.notNull;

/**
 * Universal builder for {@link Document}
 */
public class DocumentBuilder {
    private File fileContent;
    private String documentName;
    private String documentMimeType;
    private List<DataHash> hashContent;
    private EnvelopeElement elementContent;
    private ParsingStoreReference contentReference;

    public DocumentBuilder withDocumentName(String name) {
        notNull(name, "File name");
        this.documentName = name;
        return this;
    }

    public DocumentBuilder withDocumentMimeType(String mimeType) {
        notNull(mimeType, "MIME type");
        this.documentMimeType = mimeType;
        return this;
    }

    public DocumentBuilder withContent(File file) {
        notNull(file, "File");
        this.fileContent = file;
        return this;
    }

    public DocumentBuilder withDataHashList(Collection<DataHash> hashList) {
        notEmpty(hashList, "Data hash list");
        this.hashContent = new ArrayList<>(hashList);
        return this;
    }

    public DocumentBuilder withContent(EnvelopeElement element) {
        notNull(element, "EnvelopeElement");
        this.elementContent = element;
        return this;
    }

    public DocumentBuilder withParsingStoreReference(ParsingStoreReference reference) {
        notNull(reference, "Parsing store reference");
        this.contentReference = reference;
        return this;
    }

    /**
     * NB! Does not close the stream! Just reads from it.
     */
    public DocumentBuilder withContent(InputStream stream) {
        notNull(stream, "Input stream");
        this.contentReference = addToStore(stream);
        return this;
    }

    public DocumentBuilder withDocument(Document original) {
        this.documentName = original.getFileName();
        this.documentMimeType = original.getMimeType();
        // TODO: Any better option?
        if (original instanceof FileDocument) {
            return withContent(((FileDocument) original).file);
        } else if (original instanceof EmptyDocument) {
            return withDataHashList(((EmptyDocument) original).dataHashMap.values());
        } else if (original instanceof InternalDocument) {
            return withContent(((InternalDocument) original).element);
        } else {
            try (InputStream inputStream = original.getInputStream()) {
                return this.withContent(inputStream);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to access content of Document!", e);
            }
        }
    }

    public Document build() {
        if (fileContent != null) {
            return new FileDocument(fileContent, documentMimeType, documentName);
        } else if (hashContent != null) {
            return new EmptyDocument(documentName, documentMimeType, hashContent);
        } else if (elementContent != null) {
            return new InternalDocument(elementContent);
        } else if (contentReference != null) {
            return new ParsedDocument(contentReference, documentMimeType, documentName);
        }
        throw new IllegalStateException("Document content not provided!");
    }

    private static ParsingStoreReference addToStore(InputStream data) {
        notNull(data, "Input stream");
        try {
            return ActiveParsingStoreProvider.getActiveParsingStore().store(data);
        } catch (ParsingStoreException e) {
            throw new IllegalArgumentException("Can not copy input stream to memory!", e);
        }
    }

}
