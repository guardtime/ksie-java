package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.parsing.ParsingStore;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.IOException;

/**
 * This content holders is used for any file. Use as the last place to catch any unfiltered files.
 */
public class UnknownFileHandler extends ContentHandler<File> {

    public UnknownFileHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        return true;
    }

    @Override
    protected File getEntry(String name) throws ContentParsingException {
        try {
            File tmpFile = Util.createTempFile();
            Util.copyToTempFile(fetchStreamFromEntries(name), tmpFile);
            return tmpFile;
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read unknown file from stream.", e);
        }
    }

}
