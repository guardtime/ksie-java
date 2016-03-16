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

    private Pair<String, File> requestablePathFilePair;
    private Pair<String, File> unrequestedPathFilePair;
    private ContentHandler handler;

    @Mock
    private File mockFile1;

    @Mock
    private File mockFile2;

    @Before
    public void setUpEntries() throws Exception {
        requestablePathFilePair = Pair.of("name.txt", mockFile1);
        unrequestedPathFilePair = Pair.of("awesomesouce2.txt", mockFile2);
        handler = new TestContentHandler();
        handler.add(requestablePathFilePair.getLeft(), requestablePathFilePair.getRight());
        handler.add(unrequestedPathFilePair.getLeft(), unrequestedPathFilePair.getRight());
    }

    @Test
    public void testGetUnrequestedFiles() throws Exception {
        handler.get(requestablePathFilePair.getLeft());
        List<Pair<String, File>> unrequested = handler.getUnrequestedFiles();
        assertFalse(unrequested.contains(requestablePathFilePair));
        assertTrue(unrequested.contains(unrequestedPathFilePair));
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