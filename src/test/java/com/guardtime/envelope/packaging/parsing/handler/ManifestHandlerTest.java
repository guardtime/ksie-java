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

package com.guardtime.envelope.packaging.parsing.handler;

import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.ManifestFactoryType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ManifestHandlerTest {

    private static final String VALID_MANIFEST_PATH = "/META-INF/manifest-7.tlv";
    private static final String INVALID_MANIFEST_PATH = "funky_music.mp3";
    private ManifestHandler handler;

    @Mock
    protected EnvelopeManifestFactory mockManifestFactory;
    @Mock
    private ManifestFactoryType mockManifestFactoryType;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockManifestFactoryType.getManifestFileExtension()).thenReturn("tlv");
        when(mockManifestFactory.getManifestFactoryType()).thenReturn(mockManifestFactoryType);
        handler = new ManifestHandler(mockManifestFactory);
    }

    @Test
    public void testIsSupported() {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_MANIFEST_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_MANIFEST_PATH));
    }
}
