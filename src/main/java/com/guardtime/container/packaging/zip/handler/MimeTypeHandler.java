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

package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.MimeType.MIME_TYPE_ENTRY_NAME;

/**
 * This content holders is used for MIMETYPE file inside the container.
 */
public class MimeTypeHandler extends ContentHandler<byte[]> {

    public MimeTypeHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        return name.equals(MIME_TYPE_ENTRY_NAME);
    }

    @Override
    protected byte[] getEntry(String name) throws ContentParsingException {
        try (InputStream input = fetchStreamFromEntries(name)) {
            byte[] bytes = Util.toByteArray(input);
            parsingStore.remove(name);
            return bytes;
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read content of '" + name + "'", e);
        }
    }
}
