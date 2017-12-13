package com.guardtime.envelope.packaging.parsing.handler;

import java.io.InputStream;

public interface ContentHandler<T> {
    T parse(InputStream stream) throws ContentParsingException;
}
