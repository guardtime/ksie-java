package com.guardtime.container.packaging.zip.handler;

import java.io.File;

public class AnnotationContentHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "annotation[0-9]+.dat"); // All annotations are .dat
    }

    @Override
    public File getEntry(String name) {
        return entries.get(name);
    }

}
