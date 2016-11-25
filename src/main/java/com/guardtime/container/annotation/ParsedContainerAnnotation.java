package com.guardtime.container.annotation;

import com.guardtime.container.packaging.parsing.ParsedStreamProvider;
import com.guardtime.container.packaging.parsing.ParsingStoreException;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;

public class ParsedContainerAnnotation extends AbstractContainerAnnotation {

    private final ParsedStreamProvider streamProvider;

    public ParsedContainerAnnotation(ParsedStreamProvider annotationStreamProvider, String domain, ContainerAnnotationType type) {
        super(domain, type);
        notNull(annotationStreamProvider, "Input stream provider");
        this.streamProvider = annotationStreamProvider;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return streamProvider.getNewStream();
        } catch (ParsingStoreException e) {
            throw new IOException("Failed to acquire stream", e);
        }
    }

    @Override
    public void close() throws Exception {
        streamProvider.close();
    }

    @Override
    protected String getContent() {
        try(InputStream inputStream = getInputStream()) {
            byte[] bytes = Util.toByteArray(inputStream);
            return new String(bytes);
        } catch (IOException e) {
            return "";
        }
    }
}
