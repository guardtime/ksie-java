package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.util.Util;

import java.io.File;

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
