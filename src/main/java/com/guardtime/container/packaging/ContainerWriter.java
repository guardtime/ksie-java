package com.guardtime.container.packaging;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes a {@link Container} to {@link java.io.OutputStream}.
 * Implementations of this interface must be stateless and reusable.
 */
public interface ContainerWriter {
    void write(Container container, OutputStream output) throws IOException;
}
