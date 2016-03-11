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
    public byte[] getEntry(String name) {
        try {
            return Util.toByteArray(new FileInputStream(entries.get(name)));
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO proper exception handling
        }
    }
}
