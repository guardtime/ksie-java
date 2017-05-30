package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;

import java.io.IOException;

/**
 * Builds {@link ContainerPackagingFactory} for generating {@link Container} instances that use ZIP archiving for storing.
 * Will overwrite any MIME type, ContainerReader and ContainerWriter already set for builder.
 */

public class ZipContainerPackagingFactoryBuilder extends ContainerPackagingFactory.Builder {

    public static final String MIME_TYPE = "application/guardtime.ksie10+zip";

    @Override
    public ContainerPackagingFactory build() throws IOException {
        mimeType = MIME_TYPE;
        containerReader = new ZipContainerReader(manifestFactory, signatureFactory, parsingStoreFactory);
        containerWriter = new ZipContainerWriter();
        return super.build();
    }
}