package com.guardtime.container.integration;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.parsing.MemoryBasedParsingStoreFactory;
import com.guardtime.container.packaging.parsing.ParsingStoreFactory;
import com.guardtime.container.util.Util;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZipContainerMemoryBasedParsingIntegrationTest extends AbstractZipContainerIntegrationTest {

    private static final ParsingStoreFactory parsingStoreFactory = new MemoryBasedParsingStoreFactory();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setPackagingFactories(parsingStoreFactory);
    }

    @Test
    public void testNoTempFilesUsedForParsing() throws Exception {
        int ksieTempFilesCount = getTempFilesCount();
        File f = loadFile(CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS);
        try (Container c = packagingFactory.read(new FileInputStream(f))) {
            assertNotNull(c);
            assertEquals(ksieTempFilesCount, getTempFilesCount());
        }
    }

    private int getTempFilesCount() {
        int counter = 0;
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String[] list = tmpDir.toFile().list();
        if (list != null) {
            for (String str : list) {
                if (isTempFile(str)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    private boolean isTempFile(String s) {
        return s.startsWith(Util.TEMP_DIR_PREFIX) || s.startsWith(Util.TEMP_FILE_PREFIX);
    }
}
