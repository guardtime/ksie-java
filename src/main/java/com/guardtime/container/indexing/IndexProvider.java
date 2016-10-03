package com.guardtime.container.indexing;

public interface IndexProvider {

    String getNextDocumentsManifestIndex();

    String getNextManifestIndex();

    String getNextAnnotationsManifestIndex();

    String getNextSignatureIndex();

    String getNextSingleAnnotationManifestIndex();

    String getNextAnnotationIndex();
}
