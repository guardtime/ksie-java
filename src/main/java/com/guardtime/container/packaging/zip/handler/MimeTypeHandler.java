package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.ksi.util.Util;

import java.io.FileInputStream;
import java.io.IOException;

public class MimeTypeHandler extends ContentHandler<byte[]> {

    @Override
    public boolean isSupported(String name) {
        return name.equals(ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME);
    }

    @Override
    protected byte[] getEntry(String name) throws FileParsingException {
        try {
            return Util.toByteArray(new FileInputStream(entries.get(name)));
        } catch (NullPointerException | IOException e) {
            throw new FileParsingException(e);
        }
    }
}
