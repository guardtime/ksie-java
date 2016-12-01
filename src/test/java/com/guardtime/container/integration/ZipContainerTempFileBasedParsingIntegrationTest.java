package com.guardtime.container.integration;

import com.guardtime.container.packaging.parsing.ParsingStoreFactory;
import com.guardtime.container.packaging.parsing.TemporaryFileBasedParsingStoreFactory;

import org.junit.Before;

public class ZipContainerTempFileBasedParsingIntegrationTest extends AbstractZipContainerIntegrationTest {

    private static final ParsingStoreFactory parsingStoreFactory = new TemporaryFileBasedParsingStoreFactory();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setPackagingFactories(parsingStoreFactory);
    }
}
