package com.guardtime.container.packaging.zip.handler;

import java.io.File;

/**
 * This content holders is used for annotations inside the container.
 */
public class AnnotationContentHandler extends IndexedContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "annotation[0-9]+.dat"); // All annotations are .dat
    }

    @Override
    protected File getEntry(String name) {
        return entries.get(name);
    }

}
