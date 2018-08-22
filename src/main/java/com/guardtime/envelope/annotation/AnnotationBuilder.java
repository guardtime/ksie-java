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

import com.guardtime.envelope.packaging.parsing.store.ActiveParsingStoreProvider;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Universal builder for {@link Annotation}
 */
public class AnnotationBuilder {
    private String domain;
    private File fileContent;
    private String stringContent;
    private ParsingStoreReference contentReference;
    private EnvelopeAnnotationType type;
    private String path;

    public AnnotationBuilder withDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public AnnotationBuilder withAnnotationType(EnvelopeAnnotationType annotationType) {
        this.type = annotationType;
        return this;
    }

    public AnnotationBuilder withContent(File file) {
        notNull(file, "Content");
        this.fileContent = file;
        return this;
    }

    public AnnotationBuilder withContent(String content) {
        notNull(content, "Content");
        this.stringContent = content;
        return this;
    }

    public AnnotationBuilder withParsingStoreReference(ParsingStoreReference reference) {
        notNull(reference, "Parsing store reference");
        this.contentReference = reference;
        return this;
    }

    /**
     * NB! Does not close the stream! Just reads from it.
     */
    public AnnotationBuilder withContent(InputStream stream) {
        notNull(stream, "Content");
        this.contentReference = addToStore(stream);
        return this;
    }

    public AnnotationBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public AnnotationBuilder withAnnotation(Annotation original) {
        this.domain = original.getDomain();
        this.type = original.getAnnotationType();
        this.path = original.getPath();
        // TODO: Any better option?
        if (original instanceof FileAnnotation) {
            return withContent(((FileAnnotation) original).file);
        } else if (original instanceof StringAnnotation) {
            return withContent(((StringAnnotation) original).content);
        } else if (original instanceof ParsedAnnotation) {
            return withParsingStoreReference(((ParsedAnnotation) original).parsingStoreReference);
        } else {
            try (InputStream inputStream = original.getInputStream()) {
                return this.withContent(inputStream);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to access content of provided Annotation.", e);
            }
        }
    }

    public Annotation build() {
        Annotation annotation = initialBuild();
        if (path != null) {
            annotation.setPath(path);
        }
        return annotation;
    }

    private Annotation initialBuild() {
        if (fileContent != null) {
            return new FileAnnotation(fileContent, domain, type);
        } else if (stringContent != null) {
            return new StringAnnotation(stringContent, domain, type);
        } else if (contentReference != null) {
            return new ParsedAnnotation(contentReference, domain, type);
        }
        throw new IllegalStateException("Annotation content not provided!");
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
