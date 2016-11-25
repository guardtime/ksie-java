package com.guardtime.container.packaging.parsing;

import java.io.InputStream;

public interface ParsedStreamProvider extends AutoCloseable {
    InputStream getNewStream() throws ParsingStoreException;
}
