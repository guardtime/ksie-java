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

package com.guardtime.envelope.indexing;

import com.guardtime.envelope.packaging.Envelope;

/**
 * {@link IndexProvider} factory for creating correct index provider based on default values or provided {@link Envelope} as
 * baseline.
 */

public interface IndexProviderFactory {

    IndexProvider create();

    /**
     * Creates new {@link IndexProvider} based on provided envelope. Verifies the provided {@link Envelope} has indexes
     * that are supported by the created {@link IndexProvider} and if needed extracts the starting point for indexes.
     * @param envelope  The base {@link Envelope} to use for verifying index type and identifying next indexes.
     */
    IndexProvider create(Envelope envelope);
}
