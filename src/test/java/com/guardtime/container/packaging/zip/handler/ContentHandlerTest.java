package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.util.Pair;

import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

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
        Pair<String, File> requestablePathFilePair = Pair.of("name.txt", mockFile1);
        Pair<String, File> unrequestedPathFilePair = Pair.of("awesomesouce2.txt", mockFile2);

        handler.add(requestablePathFilePair.getLeft(), requestablePathFilePair.getRight());
        handler.add(unrequestedPathFilePair.getLeft(), unrequestedPathFilePair.getRight());
        handler.get(requestablePathFilePair.getLeft());

        List<Pair<String, File>> unrequested = handler.getUnrequestedFiles();
        assertFalse(unrequested.contains(requestablePathFilePair));
        assertTrue(unrequested.contains(unrequestedPathFilePair));
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