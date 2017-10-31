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

package com.guardtime.envelope.packaging.parsing.handler;

import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.packaging.EntryNameProvider.ANNOTATIONS_MANIFEST_FORMAT;

/**
 * This content holders is used for annotations manifests inside the envelope.
 */
public class AnnotationsManifestHandler extends ContentHandler<AnnotationsManifest> {

    private final EnvelopeManifestFactory manifestFactory;

    public AnnotationsManifestHandler(EnvelopeManifestFactory manifestFactory, ParsingStore store) {
        super(store);
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(ANNOTATIONS_MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected AnnotationsManifest getEntry(String name) throws ContentParsingException {
        try (InputStream input = fetchStreamFromEntries(name)) {
            AnnotationsManifest annotationsManifest = manifestFactory.readAnnotationsManifest(input, name);
            parsingStore.remove(name);
            return annotationsManifest;
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of '" + name + "'", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read content of '" + name + "'", e);
        }
    }

}
