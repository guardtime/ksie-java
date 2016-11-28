package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.io.InputStream;

/**
 * This content holders is used for MIMETYPE file inside the container.
 */
public class MimeTypeHandler extends ContentHandler<byte[]> {

    public MimeTypeHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        return name.equals(ZipContainerPackagingFactoryBuilder.MIME_TYPE_ENTRY_NAME);
    }

    @Override
    protected byte[] getEntry(String name) throws ContentParsingException {
        try (InputStream input = fetchStreamFromEntries(name)) {
            return Util.toByteArray(input);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read from file", e);
        }
    }
}
