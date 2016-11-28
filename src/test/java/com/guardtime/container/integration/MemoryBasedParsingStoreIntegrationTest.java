package com.guardtime.container.integration;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.parsing.MemoryBasedParsingStoreFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.util.Util;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MemoryBasedParsingStoreIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withParsingStoreFactory(new MemoryBasedParsingStoreFactory()).
                build();
    }

    @Test
    public void testNoTempFilesUsedForParsing() throws Exception {
        int ksieTempFilesCount = getTempFilesCount();
        File f = loadFile(CONTAINER_WITH_ONE_DOCUMENT);
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
