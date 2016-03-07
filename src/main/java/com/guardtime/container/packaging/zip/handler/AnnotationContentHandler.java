package com.guardtime.container.packaging.zip.handler;

import java.io.File;

public class AnnotationContentHandler extends ContentHandler<File> {
    private int maxIndex = 0;

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("META-INF/annotation") || name.startsWith("/META-INF/annotation"); //TODO
    }

    @Override
    public void add(String name, File file) {
        super.add(name, file);
        int index = Integer.parseInt(name.replaceAll("[^0-9]", ""));
        if(index > maxIndex) maxIndex = index;
    }

    @Override
    public File get(String name) {
        return entries.get(name);
    }

    public int getMaxIndex() {
        return maxIndex;
    }

}
