package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Container internal structure manifest that contains some meta-data and reference to annotation data.
 */
public interface SingleAnnotationManifest extends MultiHashElement {

    /**
     * Returns a reference pointing to annotation data in the container.
     */
    AnnotationDataReference getAnnotationReference();

    FileReference getDocumentsManifestReference();

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

}
