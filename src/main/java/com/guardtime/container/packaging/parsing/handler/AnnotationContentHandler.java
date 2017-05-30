package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.packaging.parsing.store.ParsingStore;

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
        if(!parsingStore.contains(name)) {
            throw new ContentParsingException("No data stored for entry '" + name + "'");
        }
        return parsingStore.get(name);
    }

}
