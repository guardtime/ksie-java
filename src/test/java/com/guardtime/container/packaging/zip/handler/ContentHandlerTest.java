package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;

import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ContentHandlerTest extends AbstractContentHandlerTest {

    @Mock
    private File mockFile2;

    @Mock
    private File mockFile1;

    private ContentHandler handler = spy(ContentHandler.class);

    @Test
    public void testAddToHandler() throws Exception {
        assertTrue(handler.getNames().isEmpty());
        handler.add("someString", mockFile1);
        assertTrue(handler.getNames().size() == 1);
    }

    @Test
    public void testGetUnrequestedFiles() throws Exception {
        String requestedFileName = "name.txt";
        String unrequestedFileName = "awesomesouce2.txt";

        handler.add(requestedFileName, mockFile1);
        handler.add(unrequestedFileName, Util.createTempFile());
        handler.get(requestedFileName);

        List unrequested = handler.getUnrequestedFiles();
        assertEquals(1, unrequested.size());
        UnknownDocument doc = (UnknownDocument) unrequested.get(0);
        assertFalse(doc.getFileName().equals(requestedFileName));
        assertTrue(doc.getFileName().equals(unrequestedFileName));
    }

    @Test
    public void testGetNames() {
        String name1 = "name.txt";
        String name2 = "awesomesouce2.txt";

        handler.add(name1, mockFile1);
        handler.add(name2, mockFile2);

        Set names = handler.getNames();

        assertTrue(names.contains(name1));
        assertTrue(names.contains(name2));
    }

}