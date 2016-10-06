package com.guardtime.container.packaging;

import com.guardtime.container.indexing.IndexProvider;

/**
 * Helper class for generating manifest, annotation data and signature URI strings.
 */
public class EntryNameProvider {

    private static final String META_INF = "META-INF/";
    public static final String DOCUMENTS_MANIFEST_FORMAT = "datamanifest-%s.%s";
    public static final String MANIFEST_FORMAT = "manifest-%s.%s";
    public static final String ANNOTATIONS_MANIFEST_FORMAT = "annotmanifest-%s.%s";
    public static final String SIGNATURE_FORMAT = "signature-%s.%s";
    public static final String SINGLE_ANNOTATION_MANIFEST_FORMAT = "annotation-%s.%s";
    public static final String ANNOTATION_DATA_FORMAT = "annotation-%s.dat";
    private final String manifestSuffix;
    private final String signatureSuffix;
    private final IndexProvider indexProvider;

    public EntryNameProvider(String manifestSuffix, String signatureSuffix, IndexProvider indexProvider) {
        this.manifestSuffix = manifestSuffix;
        this.signatureSuffix = signatureSuffix;
        this.indexProvider = indexProvider;
    }

    public String nextDocumentsManifestName() {
        return String.format(META_INF + DOCUMENTS_MANIFEST_FORMAT, indexProvider.getNextDocumentsManifestIndex(), manifestSuffix);
    }

    public String nextManifestName() {
        return String.format(META_INF + MANIFEST_FORMAT, indexProvider.getNextManifestIndex(), manifestSuffix);
    }

    public String nextAnnotationsManifestName() {
        return String.format(META_INF + ANNOTATIONS_MANIFEST_FORMAT, indexProvider.getNextAnnotationsManifestIndex(), manifestSuffix);
    }

    public String nextSignatureName() {
        return String.format(META_INF + SIGNATURE_FORMAT, indexProvider.getNextSignatureIndex(), signatureSuffix);
    }

    public String nextSingleAnnotationManifestName() {
        return String.format(META_INF + SINGLE_ANNOTATION_MANIFEST_FORMAT, indexProvider.getNextSingleAnnotationManifestIndex(), manifestSuffix);
    }

    public String nextAnnotationDataFileName() {
        return String.format(META_INF + ANNOTATION_DATA_FORMAT, indexProvider.getNextAnnotationIndex());
    }
}
