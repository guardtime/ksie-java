package com.guardtime.container.integration;

import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.parsing.ParsingStoreFactory;
import com.guardtime.container.packaging.parsing.TemporaryFileBasedParsingStoreFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Before;

public class ZipContainerTempFileBasedParsingIntegrationTest extends AbstractZipContainerIntegrationTest {

    private static final ParsingStoreFactory parsingStoreFactory = new TemporaryFileBasedParsingStoreFactory();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setUpPackagingFactories();
    }

    private void setUpPackagingFactories() {

        this.packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withParsingStoreFactory(parsingStoreFactory).
                withSignatureFactory(signatureFactory)
                .build();

        this.packagingFactoryWithIncIndex = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                withParsingStoreFactory(parsingStoreFactory)
                .build();
        this.packagingFactoryWithUuid = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                withParsingStoreFactory(parsingStoreFactory)
                .build();
    }
}
