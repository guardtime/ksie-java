package com.guardtime.container.packaging.parsing;

public interface ParsingStoreFactory {
    ParsingStore build() throws ParsingStoreException;
}
