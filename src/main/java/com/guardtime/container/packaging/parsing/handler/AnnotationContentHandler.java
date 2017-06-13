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

package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.packaging.parsing.store.ParsingStore;

import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.ANNOTATION_DATA_FORMAT;

/**
 * This content holders is used for annotations inside the container.
 */
public class AnnotationContentHandler extends ContentHandler<InputStream> {

    public AnnotationContentHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(ANNOTATION_DATA_FORMAT, ".+");
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected InputStream getEntry(String name) throws ContentParsingException {
        if(!parsingStore.contains(name)) {
            throw new ContentParsingException("No data stored for entry '" + name + "'");
        }
        return parsingStore.get(name);
    }

}
