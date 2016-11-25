package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class UnknownFileHandlerTest extends AbstractContentHandlerTest {

    @Before
    public void setUpHandler() {
        handler = new UnknownFileHandler(mockStore);
    }

    @Test
    public void testIsSupportedReturnsTrueForAnyString() throws Exception {
        assertTrue(handler.isSupported(UUID.randomUUID().toString()));
    }

}