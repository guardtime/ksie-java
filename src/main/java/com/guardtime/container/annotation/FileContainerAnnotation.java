package com.guardtime.container.annotation;

import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Annotation that is based on File as the data source.
 */
public class FileContainerAnnotation extends AbstractContainerAnnotation {

    private final File file;

    public FileContainerAnnotation(File file, String domain, ContainerAnnotationType type) {
        super(domain, type);
        Util.notNull(file, "File");
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
