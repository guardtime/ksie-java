package com.guardtime.envelope.packaging.parsing.store;

import java.io.InputStream;
import java.util.UUID;

/**
 * Interactor to ParsingStore.
 */
public class ParsingStoreReference {
    private final UUID uuid;
    private final String name;
    private final ParsingStore owner;

    public ParsingStoreReference(UUID uuid, String name, ParsingStore store) {
        this.uuid = uuid;
        this.name = name;
        this.owner = store;
    }

    public String getName() {
        return name;
    }

    public InputStream get() {
        return owner.get(uuid);
    }

    public ParsingStoreReference clone() {
        return owner.addNewReference(uuid, name);
    }

    public void unstore() {
        owner.unregister(uuid, this);
    }
}
