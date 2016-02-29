package com.guardtime.container.packaging.zip.handler;

import java.io.File;

public class AnnotationContentHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("META-INF/annotation") || name.startsWith("/META-INF/annotation"); //TODO
    }

    @Override
    public File get(String name) {
        return entries.get(name);
    }

}
