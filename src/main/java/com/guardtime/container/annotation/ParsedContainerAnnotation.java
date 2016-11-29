package com.guardtime.container.annotation;

import com.guardtime.container.packaging.parsing.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;

/**
 * Represents a {@link ContainerAnnotation} that has been parsed in. Uses a {@link ParsingStore} from where to access the data of
 * the {@link ContainerAnnotation}
 */
public class ParsedContainerAnnotation extends AbstractContainerAnnotation {

    private final ParsingStore parsingStore;
    private final String key;

    public ParsedContainerAnnotation(ParsingStore store, String key, String domain, ContainerAnnotationType type) {
        super(domain, type);
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

    @Override
    public void close() throws Exception {
        parsingStore.remove(key);
    }
}
