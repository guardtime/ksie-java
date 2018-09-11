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
public final class AnnotationFactory {

    private AnnotationFactory() {
        //
    }

    public static Annotation create(File file, String domain, EnvelopeAnnotationType type) {
        return new FileAnnotation(file, domain, type);
    }

    public static Annotation create(String content, String domain, EnvelopeAnnotationType type) {
        return new StringAnnotation(content, domain, type);
    }

    public static Annotation create(ParsingStoreReference reference, String domain, EnvelopeAnnotationType type) {
        return new ParsedAnnotation(reference, domain, type);
    }

    /**
     * NB! Does not close the stream! Just reads from it.
     */
    public static Annotation create(InputStream stream, String domain, EnvelopeAnnotationType type) {
        return create(addToStore(stream), domain, type);
    }

    public static Annotation create(Annotation original) {
        // TODO: Any better option?
        Annotation newAnnotation = null;
        if (original instanceof FileAnnotation) {
            newAnnotation = create(((FileAnnotation) original).file, original.getDomain(), original.getAnnotationType());
        } else if (original instanceof StringAnnotation) {
            newAnnotation = create(((StringAnnotation) original).content, original.getDomain(), original.getAnnotationType());
        } else if (original instanceof ParsedAnnotation) {
            newAnnotation = create(
                    ((ParsedAnnotation) original).parsingStoreReference,
                    original.getDomain(),
                    original.getAnnotationType()
            );
        } else {
            try (InputStream inputStream = original.getInputStream()) {
                newAnnotation = create(inputStream, original.getDomain(), original.getAnnotationType());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to access content of provided Annotation.", e);
            }
        }
        newAnnotation.setPath(original.getPath());
        return newAnnotation;
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
