package com.guardtime.container.document;

public interface UnknownDocument extends ContainerDocument {

    /**
     * Returns a deep copied UnknownDocument or null if clone fails.
     */
    UnknownDocument clone();
}
