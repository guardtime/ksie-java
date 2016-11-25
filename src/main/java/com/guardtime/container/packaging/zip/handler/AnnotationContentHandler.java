package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.parsing.ParsedStreamProvider;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.parsing.ParsingStoreException;

import static com.guardtime.container.packaging.EntryNameProvider.ANNOTATION_DATA_FORMAT;

/**
 * This content holders is used for annotations inside the container.
 */
public class AnnotationContentHandler extends ContentHandler<ParsedStreamProvider> {

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
    protected ParsedStreamProvider getEntry(String name) throws ContentParsingException {
        try {
            return parsingStore.getParsedStreamProvider(name);
        } catch (ParsingStoreException e) {
            throw new ContentParsingException("Failed to get annotation content", e);
        }
    }

}
