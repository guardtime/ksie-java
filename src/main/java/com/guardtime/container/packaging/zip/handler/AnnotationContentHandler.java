package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.parsing.ParsingStore;

import static com.guardtime.container.packaging.EntryNameProvider.ANNOTATION_DATA_FORMAT;

/**
 * This content holders is used for annotations inside the container.
 */
public class AnnotationContentHandler extends ContentHandler<ParsingStore> {

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
    protected ParsingStore getEntry(String name) throws ContentParsingException {
        if(parsingStore.get(name) == null) {
            throw new ContentParsingException("No data stored for entry '" + name + "'");
        }
        return parsingStore;
    }

}
