package com.guardtime.container.manifest;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Container structure manifest containing references to documents contained in the container.
 */
public interface DocumentsManifest extends MultiHashElement {

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

    List<? extends FileReference> getDocumentReferences();

}
