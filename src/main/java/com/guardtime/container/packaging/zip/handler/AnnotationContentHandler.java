package com.guardtime.container.packaging.zip.handler;

import java.io.File;

public class AnnotationContentHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameStartsWith(name, "annotation") &&
                name.endsWith(".dat"); // All annotations are .dat
    }

    @Override
    public File get(String name) {
        return entries.get(name);
    }

}
