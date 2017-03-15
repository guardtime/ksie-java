package com.guardtime.container.indexing;

import com.guardtime.container.packaging.Container;

public interface IndexProviderFactory {

    IndexProvider create();

    /**
     * Creates new {@link IndexProvider} based on provided {@param container}. Verifies the provided {@link Container} has indexes
     * that are supported by the created {@link IndexProvider} and if needed extracts the starting point for indexes.
     */
    IndexProvider create(Container container);
}
