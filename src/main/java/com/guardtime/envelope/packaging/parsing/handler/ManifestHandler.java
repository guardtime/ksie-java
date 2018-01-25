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

package com.guardtime.envelope.packaging.parsing.handler;

import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.manifest.Manifest;

import java.io.InputStream;
import java.util.regex.Pattern;

import static com.guardtime.envelope.packaging.EntryNameProvider.MANIFEST_FORMAT;

/**
 * This content holders is used for manifests inside the envelope.
 */
public class ManifestHandler implements ContentHandler<Manifest> {

    private final EnvelopeManifestFactory manifestFactory;
    private final Pattern pattern;

    public ManifestHandler(EnvelopeManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
        this.pattern = Pattern.compile(String.format(
                "/?" + MANIFEST_FORMAT.replaceAll("([\\.])", "\\\\$1"),
                ".+",
                manifestFactory.getManifestFactoryType().getManifestFileExtension()
        ));
    }

    public boolean isSupported(String name) {
        return pattern.matcher(name).matches();
    }

    @Override
    public Manifest parse(InputStream input, String path) throws ContentParsingException {
        try {
            return manifestFactory.readManifest(input, path);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of stream as a Manifest.", e);
        }
    }

}
