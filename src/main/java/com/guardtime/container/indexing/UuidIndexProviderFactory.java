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

package com.guardtime.container.indexing;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Creates a {@link IndexProvider} that produces random {@link UUID} strings for each output.
 */
public class UuidIndexProviderFactory implements IndexProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(UuidIndexProviderFactory.class);

    @Override
    public IndexProvider create() {
        return new UuidIndexProvider();
    }

    @Override
    public IndexProvider create(Container container) {
        for (SignatureContent signatureContent : container.getSignatureContents()) {
            Set<String> uris = getUriSet(signatureContent);
            verifyUuidExistence(uris);
        }
        return new UuidIndexProvider();
    }

    private Set<String> getUriSet(SignatureContent signatureContent) {
        Set<String> uris = new HashSet<>();

        Manifest manifest = signatureContent.getManifest().getRight();
        if (manifest != null && manifest.getSignatureReference() != null) {
            uris.add(manifest.getSignatureReference().getUri());
        }
        uris.add(signatureContent.getManifest().getLeft());
        uris.add(signatureContent.getDocumentsManifest().getLeft());
        uris.add(signatureContent.getAnnotationsManifest().getLeft());
        uris.addAll(signatureContent.getSingleAnnotationManifests().keySet());
        uris.addAll(signatureContent.getAnnotations().keySet());
        return uris;
    }

    private void verifyUuidExistence(Set<String> set) {
        for (String str : set) {
            str = str.substring(str.lastIndexOf("/") + 1);
            String index = str.substring(str.indexOf("-") + 1, str.lastIndexOf("."));
            try {
                UUID.fromString(index);
            } catch (IllegalArgumentException e) {
                logger.warn("Not a RFC4122 UUID based index");
            }
        }
    }

    class UuidIndexProvider implements IndexProvider {

        UuidIndexProvider() {
        }

        @Override
        public String getNextDocumentsManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextAnnotationsManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextSignatureIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextSingleAnnotationManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextAnnotationIndex() {
            return UUID.randomUUID().toString();
        }
    }
}
