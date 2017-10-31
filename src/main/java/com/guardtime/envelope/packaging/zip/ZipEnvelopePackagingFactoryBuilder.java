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

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;

import java.io.IOException;

/**
 * Builds {@link EnvelopePackagingFactory} for generating {@link Envelope} instances that use ZIP archiving for storing.
 * Will overwrite any MIME type, EnvelopeReader and EnvelopeWriter already set for builder.
 */

public class ZipEnvelopePackagingFactoryBuilder extends EnvelopePackagingFactory.Builder {

    public static final String MIME_TYPE = "application/guardtime.ksie10+zip";

    @Override
    public EnvelopePackagingFactory build() throws IOException {
        envelopeReader = new ZipEnvelopeReader(manifestFactory, signatureFactory, parsingStoreFactory);
        envelopeWriter = new ZipEnvelopeWriter();
        return super.build();
    }
}