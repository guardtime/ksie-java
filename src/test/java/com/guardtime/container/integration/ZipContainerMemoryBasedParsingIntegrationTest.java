package com.guardtime.container.integration;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.parsing.store.MemoryBasedParsingStoreFactory;
import com.guardtime.container.packaging.parsing.store.ParsingStoreFactory;
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

    private ContainerPackagingFactory packagingFactory;

    @Before
    public void setUpPackagingFactories() throws Exception {
        super.setUpPackagingFactories();
        packagingFactory = getDefaultPackagingFactory();
    }

    @Override
    public ParsingStoreFactory getParsingStoreFactory() {
        return new MemoryBasedParsingStoreFactory();
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
