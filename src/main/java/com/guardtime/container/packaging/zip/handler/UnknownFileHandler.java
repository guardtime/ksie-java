package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.document.UnknownDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * This content holders is used for any file. Use as the last place to catch any unfiltered files.
 */
public class UnknownFileHandler extends ContentHandler<UnknownDocument> {

    @Override
    public boolean isSupported(String name) {
        return true;
    }

    @Override
    protected UnknownDocument getEntry(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (InputStream inputStream = new FileInputStream(file)) {
            StreamContainerDocument unknown = new StreamContainerDocument(inputStream, "unknown", name);
            Files.deleteIfExists(file.toPath());
            return unknown;
        } catch (IOException e) {
            throw new ContentParsingException("Failed to parse unknown file.", e);
        }
    }

}
