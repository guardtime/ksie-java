package com.guardtime.container.integration;

import com.guardtime.container.packaging.parsing.ParsingStoreFactory;
import com.guardtime.container.packaging.parsing.TemporaryFileBasedParsingStoreFactory;

public class ZipContainerTempFileBasedParsingIntegrationTest extends AbstractZipContainerIntegrationTest {

    private static final ParsingStoreFactory parsingStoreFactory = new TemporaryFileBasedParsingStoreFactory();

    @Override
    protected ParsingStoreFactory getParsingStoreFactory() {
        return parsingStoreFactory;
    }
}
