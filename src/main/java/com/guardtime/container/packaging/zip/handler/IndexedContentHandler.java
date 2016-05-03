package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.util.Util;

import java.io.File;

/**
 * Helper class for reading specific type of zip file entry. To check if zip entry can be used by current handler use
 * the {@link ContentHandler#isSupported(String)} method.
 * <p/>
 * Keeps that of the integer index in file path and stores the maximum value it has encountered.
 *
 * @param <T>
 *         type of the entry
 */
abstract class IndexedContentHandler<T> extends ContentHandler<T> {
    private int maxIndex;

    @Override
    public void add(String name, File file) {
        super.add(name, file);
        updateMaxIndex(name);
    }

    protected void updateMaxIndex(String name) {
        int index = Util.extractIntegerFrom(name);
        if (index > maxIndex) maxIndex = index;
    }

    public int getMaxIndex() {
        return maxIndex;
    }
}
