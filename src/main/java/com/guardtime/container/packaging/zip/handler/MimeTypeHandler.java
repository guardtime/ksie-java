package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.ksi.util.Util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MimeTypeHandler extends ContentHandler<byte[]> {

    @Override
    public boolean isSupported(String name) {
        return name.equals(ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME);
    }

    @Override
    public byte[] get(String name) throws ContentParsingException {
        try {
            return Util.toByteArray(new FileInputStream(entries.get(name)));
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read from file", e);
        }
    }
}
