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

    private Pair<String, File> SAMPLE_PAIR_ONE;
    private Pair<String, File> SAMPLE_PAIR_TWO;
    private ContentHandler handler;

    @Mock
    File mockFile1;

    @Mock
    File mockFile2;

    @Before
    public void setUpEntries() throws Exception {
        SAMPLE_PAIR_ONE = Pair.of("name.txt", mockFile1);
        SAMPLE_PAIR_TWO = Pair.of("awesomesouce2.txt", mockFile2);
        handler = new TestContentHandler();
        handler.add(SAMPLE_PAIR_ONE.getLeft(), SAMPLE_PAIR_ONE.getRight());
        handler.add(SAMPLE_PAIR_TWO.getLeft(), SAMPLE_PAIR_TWO.getRight());
    }

    @Test
    public void testGetUnrequestedFiles() throws Exception {
        handler.get(SAMPLE_PAIR_ONE.getLeft());
        List<Pair<String, File>> unrequested = handler.getUnrequestedFiles();
        assertFalse(unrequested.contains(SAMPLE_PAIR_ONE));
        assertTrue(unrequested.contains(SAMPLE_PAIR_TWO));
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