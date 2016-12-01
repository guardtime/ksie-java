package com.guardtime.container.integration;

import com.guardtime.container.packaging.parsing.ParsingStoreFactory;
import com.guardtime.container.packaging.parsing.TemporaryFileBasedParsingStoreFactory;

public class ZipContainerTempFileBasedParsingIntegrationTest extends AbstractZipContainerIntegrationTest {

    @Override
    protected ParsingStoreFactory getParsingStoreFactory() {
        return new TemporaryFileBasedParsingStoreFactory();
    }
}
