package com.guardtime.container.packaging.zip.handler;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class UnknownFileHandlerTest {

    @Test
    public void testIsSupportedReturnsTrueForAnyString() throws Exception {
        ContentHandler handler = new UnknownFileHandler();
        assertTrue(handler.isSupported(UUID.randomUUID().toString()));
    }

}