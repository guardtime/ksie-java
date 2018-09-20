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

package com.guardtime.envelope.packaging.parsing.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Interactor to ParsingStore.
 */
public class ParsingStoreReference {
    private final UUID uuid;
    private final ParsingStore owner;
    private final String pathName;

    public ParsingStoreReference(UUID uuid, ParsingStore store, String pathName) {
        this.uuid = uuid;
        this.owner = store;
        this.pathName = pathName;
    }

    public ParsingStoreReference(ParsingStoreReference original) {
        this(original.uuid, original.owner, original.pathName);
        owner.updateReferences(uuid, this);
    }

    public InputStream getStoredContent() throws IOException {
        InputStream inputStream = owner.getContent(uuid);
        if (inputStream == null) {
            throw new IOException(
                    "Failed to acquire input stream from parsing store for key '" + getKey() + "'"
            );
        }
        return inputStream;
    }

    public void unstore() {
        owner.unregister(uuid, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getKey() {
        String result = getUuid().toString();
        if (pathName != null) {
            return result + " - " + pathName;
        }
        return result;
    }
}
