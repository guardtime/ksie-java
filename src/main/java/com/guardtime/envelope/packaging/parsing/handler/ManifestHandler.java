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

import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.manifest.Manifest;

import java.io.InputStream;

import static com.guardtime.envelope.packaging.EntryNameProvider.MANIFEST_FORMAT;

/**
 * This content holders is used for manifests inside the envelope.
 */
public class ManifestHandler implements ContentHandler<Manifest> {

    private final EnvelopeManifestFactory manifestFactory;

    public ManifestHandler(EnvelopeManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    public boolean isSupported(String name) {
        String regex = String.format(MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    private boolean matchesSingleDirectory(String str, String dirName) {
        return str.matches("/?" + dirName + "/[^/]*");
    }

    private boolean fileNameMatches(String str, String regex) {
        int startingIndex = str.startsWith("/") ? 1 : 0;
        return str.substring(startingIndex).matches(regex);
    }

    @Override
    public Manifest parse(InputStream input) throws ContentParsingException {
        try {
            return manifestFactory.readManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of stream as a Manifest.", e);
        }
    }

}
