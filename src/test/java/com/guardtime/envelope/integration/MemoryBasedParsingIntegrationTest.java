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

package com.guardtime.envelope.integration;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.parsing.store.MemoryBasedParsingStoreFactory;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.envelope.util.Util;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MemoryBasedParsingIntegrationTest extends AbstractEnvelopeParsingIntegrationTest {

    private EnvelopePackagingFactory packagingFactory;

    @Before
    public void setUpPackagingFactories() throws Exception {
        super.setUpPackagingFactories();
        packagingFactory = getDefaultPackagingFactory();
    }

    @Override
    public ParsingStoreFactory getParsingStore() {
        return new MemoryBasedParsingStoreFactory();
    }

    @Test
    public void testNoTempFilesUsedForParsing() throws Exception {
        int ksieTempFilesCount = getTempFilesCount();
        File f = loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS);
        try (Envelope c = packagingFactory.read(new FileInputStream(f))) {
            assertNotNull(c);
            assertEquals(ksieTempFilesCount, getTempFilesCount());
        }
    }

    private int getTempFilesCount() {
        int counter = 0;
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String[] list = tmpDir.toFile().list();
        if (list != null) {
            for (String str : list) {
                if (isTempFile(str)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    private boolean isTempFile(String s) {
        return s.startsWith(Util.TEMP_DIR_PREFIX) || s.startsWith(Util.TEMP_FILE_PREFIX);
    }
}
