package com.guardtime.container.integration;

import com.guardtime.container.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.container.packaging.parsing.store.TemporaryFileBasedParsingStoreFactory;

public class ZipContainerTempFileBasedParsingIntegrationTest extends AbstractZipContainerIntegrationTest {

    @Override
    protected ParsingStoreFactory getParsingStoreFactory() {
        return new TemporaryFileBasedParsingStoreFactory();
    }
}
