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

import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.ManifestFactoryType;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.TemporaryFileBasedParsingStoreFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.guardtime.ksi.util.Util.toByteArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public abstract class AbstractContentHandlerTest {

    @Mock
    protected EnvelopeManifestFactory mockManifestFactory;
    @Mock
    private ManifestFactoryType mockManifestFactoryType;

    protected ContentHandler handler;
    protected ParsingStore store;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockManifestFactoryType.getManifestFileExtension()).thenReturn("tlv");
        when(mockManifestFactory.getManifestFactoryType()).thenReturn(mockManifestFactoryType);
        this.store = new TemporaryFileBasedParsingStoreFactory().create();
    }

    @Test
    public void testGetUnrequestedFiles() throws Exception {
        String requestedFileName = "name.txt";
        String requestedStreamContent = "SomeStreamOne";

        String unrequestedFileName = "awesomesouce2.txt";
        String unrequestedStreamContent = "TooAwesomeToBeUsed";

        InputStream mockStream = new ByteArrayInputStream(requestedStreamContent.getBytes(StandardCharsets.UTF_8));
        handler.add(requestedFileName, mockStream);
        mockStream.close();
        mockStream = new ByteArrayInputStream(unrequestedStreamContent.getBytes(StandardCharsets.UTF_8));
        handler.add(unrequestedFileName, mockStream);
        Object result = handler.get(requestedFileName);
        if (result instanceof Closeable) { // Some handlers return a resource that needs to be closed
            ((Closeable) result).close();
        }

        List unrequested = handler.getUnrequestedFiles();
        assertEquals(1, unrequested.size());
        UnknownDocument doc = (UnknownDocument) unrequested.get(0);
        assertFalse(doc.getFileName().equals(requestedFileName));
        assertTrue(doc.getFileName().equals(unrequestedFileName));
        try(InputStream inputStream = doc.getInputStream()) {
            assertTrue(Arrays.equals(unrequestedStreamContent.getBytes(StandardCharsets.UTF_8), toByteArray(inputStream)));
        }
    }

    @Test
    public void testGetNames() throws Exception {
        String name1 = "name.txt";
        String name2 = "awesomesouce2.txt";

        handler.add(name1, Mockito.mock(InputStream.class));
        handler.add(name2, Mockito.mock(InputStream.class));

        Set names = handler.getNames();

        assertTrue(names.contains(name1));
        assertTrue(names.contains(name2));
    }

}
