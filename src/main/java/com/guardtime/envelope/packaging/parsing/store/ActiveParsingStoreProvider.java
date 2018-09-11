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

/**
 * Provides access to instance of {@link ParsingStore} which has been set as active.
 */
public final class ActiveParsingStoreProvider {
    private static ActiveParsingStoreProvider instance;
    private ParsingStore storeInstance;

    private ActiveParsingStoreProvider() {
        // private!
    }

    public static void setActiveParsingStore(ParsingStore store) {
        getInstance().storeInstance = store;
    }

    /**
     * Return instance of {@link ParsingStore} that was set as active or defaults to instance of {@link MemoryBasedParsingStore}
     * or {@link TemporaryFileBasedParsingStore} which has existing instance. Fallback result is {@link MemoryBasedParsingStore}.
     */
    public static ParsingStore getActiveParsingStore() {
        if (getInstance().storeInstance == null) {
            if (MemoryBasedParsingStore.isInstanciated()) {
                setActiveParsingStore(MemoryBasedParsingStore.getInstance());
            } else if (TemporaryFileBasedParsingStore.isInstantiated()) {
                setActiveParsingStore(TemporaryFileBasedParsingStore.getInstance());
            } else {
                setActiveParsingStore(MemoryBasedParsingStore.getInstance());
            }
        }
        return getInstance().storeInstance;
    }

    private static ActiveParsingStoreProvider getInstance() {
        if (instance == null) {
            instance = new ActiveParsingStoreProvider();
        }
        return instance;
    }
}
