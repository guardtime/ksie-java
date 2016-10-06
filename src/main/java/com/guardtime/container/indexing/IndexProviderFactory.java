package com.guardtime.container.indexing;

import com.guardtime.container.packaging.Container;

public interface IndexProviderFactory {

    IndexProvider create() throws IndexingException;

    IndexProvider create(Container container) throws IndexingException;
}
