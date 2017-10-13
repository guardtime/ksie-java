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

import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SignatureHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_SIGNATURE_PATH = "/META-INF/signature-1.ksig";
    private static final String INVALID_SIGNATURE_PATH = "funky_music.mp3";

    @Mock
    private SignatureFactory mockSignatureFactory;

    @Mock
    private SignatureFactoryType mockFactoryType;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockFactoryType.getSignatureFileExtension()).thenReturn("ksig");
        when(mockSignatureFactory.getSignatureFactoryType()).thenReturn(mockFactoryType);
        handler = new SignatureHandler(mockSignatureFactory, store);
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_SIGNATURE_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_SIGNATURE_PATH));
    }
}