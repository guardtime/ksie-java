package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContentHandlerTest extends AbstractContentHandlerTest {

    private Pair<String, File> REQUESTABLE_PATH_FILE_PAIR;
    private Pair<String, File> UNREQUESTED_PATH_FILE_PAIR;
    private ContentHandler handler;

    @Mock
    private File mockFile1;

    @Mock
    private File mockFile2;

    @Before
    public void setUpEntries() throws Exception {
        REQUESTABLE_PATH_FILE_PAIR = Pair.of("name.txt", mockFile1);
        UNREQUESTED_PATH_FILE_PAIR = Pair.of("awesomesouce2.txt", mockFile2);
        handler = new TestContentHandler();
        handler.add(REQUESTABLE_PATH_FILE_PAIR.getLeft(), REQUESTABLE_PATH_FILE_PAIR.getRight());
        handler.add(UNREQUESTED_PATH_FILE_PAIR.getLeft(), UNREQUESTED_PATH_FILE_PAIR.getRight());
    }

    @Test
    public void testGetUnrequestedFiles() throws Exception {
        handler.get(REQUESTABLE_PATH_FILE_PAIR.getLeft());
        List<Pair<String, File>> unrequested = handler.getUnrequestedFiles();
        assertFalse(unrequested.contains(REQUESTABLE_PATH_FILE_PAIR));
        assertTrue(unrequested.contains(UNREQUESTED_PATH_FILE_PAIR));
    }

    private class TestContentHandler extends ContentHandler<File> {

        @Override
        public boolean isSupported(String name) {
            return true;
        }

        @Override
        public File getEntry(String name) {
            return entries.get(name);
        }
    }
}