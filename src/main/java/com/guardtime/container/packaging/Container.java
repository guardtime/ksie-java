package com.guardtime.container.packaging;

import com.guardtime.container.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Container that encompasses documents, annotations and structure elements that links the annotations to the documents
 * and signatures that validate the content of the container.
 */
public interface Container {
    /**
     * @return Ordered list of SignatureContent where the order is ascending based on the index of manifest
     */
    List<? extends SignatureContent> getSignatureContents();

    void writeTo(OutputStream output) throws IOException;

    MimeType getMimeType();

    /**
     * @return List of all {@link File} that were not associated with any structure elements or signatures but were
     * contained in the {@link Container}
     */
    //TODO: Replace File with InputStream or similar more abstract concept as to not limit the container to be File based
    List<Pair<String, File>> getUnknownFiles();

}
