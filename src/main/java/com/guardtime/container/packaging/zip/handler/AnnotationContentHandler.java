package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.parsing.ParsingStore;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.ANNOTATION_DATA_FORMAT;

/**
 * This content holders is used for annotations inside the container.
 */
public class AnnotationContentHandler extends ContentHandler<InputStream> {

    public AnnotationContentHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(ANNOTATION_DATA_FORMAT, ".+");
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected InputStream getEntry(String name) throws ContentParsingException {
        return fetchStreamFromEntries(name);
    }

}
