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

package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.parsing.EnvelopeReader;
import com.guardtime.envelope.packaging.parsing.HandlerSet;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.envelope.signature.SignatureFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for reading ZIP envelope.
 */
class ZipEnvelopeReader extends EnvelopeReader {

    ZipEnvelopeReader(EnvelopeManifestFactory manifestFactory, SignatureFactory signatureFactory,
                      ParsingStoreFactory storeFactory) throws IOException {
        super(manifestFactory, signatureFactory, storeFactory);
    }

    protected ZipEnvelopeWriter getWriter() {
        return new ZipEnvelopeWriter();
    }

    protected void parseInputStream(InputStream input, HandlerSet handlerSet, EnvelopeReadingException readingException)
            throws IOException {
        try (ZipInputStream zipInput = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) {
                    LOGGER.trace("Skipping ZIP directory '{}'", name);
                    continue;
                }
                try {
                    LOGGER.debug("Reading ZIP entry '{}'.", name);
                    handlerSet.add(name, zipInput);
                } catch (ParsingStoreException e) {
                    readingException.addException(e);
                }
            }
        }
    }

}