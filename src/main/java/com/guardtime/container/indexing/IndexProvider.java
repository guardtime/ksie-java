package com.guardtime.container.indexing;

import com.guardtime.container.packaging.Container;

public interface IndexProvider {

    /**
     * Updates self to provide correct next indexes based on provided {@param container}
     * @throws IndexingException when the indexes encountered in provided container are not recognized.
     */
    void updateIndexes(Container container) throws IndexingException;

    String getNextDocumentsManifestIndex();

    String getNextManifestIndex();

    String getNextAnnotationsManifestIndex();

    String getNextSignatureIndex();

    String getNextSingleAnnotationManifestIndex();

    String getNextAnnotationIndex();
}
