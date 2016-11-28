package com.guardtime.container.document;

import com.guardtime.container.packaging.parsing.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;

/**
 * Represents a {@link ContainerDocument} that has been parsed in. Uses a {@link ParsingStore} from where to access the data of
 * the {@link ContainerDocument}
 */
public class ParsedContainerDocument extends AbstractContainerDocument implements UnknownDocument {

    private final ParsingStore parsingStore;
    private final String key;

    public ParsedContainerDocument(ParsingStore store, String key, String mimeType, String fileName) {
        super(mimeType, fileName);
        notNull(store, "Parsing store");
        notNull(key, "Parsing store key");
        this.parsingStore = store;
        this.key = key;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = parsingStore.get(key);
        if (inputStream == null) {
            throw new IOException("Failed to acquire stream");
        }
        return inputStream;
    }
}
