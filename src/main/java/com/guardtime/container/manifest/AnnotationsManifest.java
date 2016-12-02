package com.guardtime.container.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Container structure manifest containing references to {@link SingleAnnotationManifest} contained in the container.
 */
public interface AnnotationsManifest extends MultiHashElement {

    List<? extends FileReference> getSingleAnnotationManifestReferences();

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

}
