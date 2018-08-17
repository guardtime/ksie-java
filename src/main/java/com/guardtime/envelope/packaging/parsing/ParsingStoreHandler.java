package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.document.DocumentBuilder;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParsingStoreHandler {
    private final ParsingStore store;
    private final Map<String, ParsingStoreReference> references = new HashMap<>();
    private final Set<String> requestedKeys = new HashSet<>();

    public ParsingStoreHandler(ParsingStore store) {
        this.store = store;
    }

    public List<UnknownDocument> getUnrequestedFiles() {
        List<UnknownDocument> returnable = new ArrayList<>();
        List<String> keys = new ArrayList<>(references.keySet());
        keys.removeAll(requestedKeys);
        for (String key : keys) {
            returnable.add(
                    (UnknownDocument) new DocumentBuilder()
                            .withDocumentMimeType("unknown")
                            .withDocumentName(key)
                            .withParsingStoreReference(get(key))
                            .build());
        }
        return returnable;
    }

    public List<String> getStoredKeys() {
        return new ArrayList<>(references.keySet());
    }

    public boolean contains(String path) {
        return references.containsKey(path);
    }

    public ParsingStoreReference get(String path) {
        requestedKeys.add(path);
        return new ParsingStoreReference(references.get(path));
    }

    public void store(String name, InputStream input) throws ParsingStoreException {
        ParsingStoreReference ref = store.store(name, input);
        references.put(name, ref);
    }

    /**
     * Clear all {@link ParsingStoreReference}s created during storing. Should be called once all necessary references have been
     * used.
     */
    public void clear() {
        for (ParsingStoreReference reference: references.values()) {
            reference.unstore();
        }
    }
}
