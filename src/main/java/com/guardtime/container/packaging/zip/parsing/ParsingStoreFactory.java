package com.guardtime.container.packaging.zip.parsing;

public interface ParsingStoreFactory {
    ParsingStore build() throws ParsingStoreException;
}
