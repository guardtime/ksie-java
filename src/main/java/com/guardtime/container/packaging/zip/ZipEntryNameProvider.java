package com.guardtime.container.packaging.zip;

/**
 * Helper class for generating zip file entry names.
 */
class ZipEntryNameProvider {

    private final String manifestSuffix;
    private final String signatureSuffix;

    int dataManifestIndex = 0;
    int manifestIndex = 0;
    int signatureIndex = 0;
    int annotationsManifestIndex = 0;
    int annotationManifestIndex = 0;
    int annotationIndex = 0;

    ZipEntryNameProvider(String manifestSuffix, String signatureSuffix) {
        this.manifestSuffix = manifestSuffix;
        this.signatureSuffix = signatureSuffix;
    }

    public ZipEntryNameProvider(String manifestSuffix, String signatureSuffix, int dataManifestIndex, int manifestIndex, int signatureIndex, int annotationsManifestIndex, int annotationManifestIndex, int annotationIndex) {
        this(manifestSuffix, signatureSuffix);
        this.dataManifestIndex = dataManifestIndex;
        this.manifestIndex = manifestIndex;
        this.signatureIndex = signatureIndex;
        this.annotationsManifestIndex = annotationsManifestIndex;
        this.annotationManifestIndex = annotationManifestIndex;
        this.annotationIndex = annotationIndex;
    }

    public String nextDataManifestName() {
        return String.format("/META-INF/datamanifest%d.%s", ++dataManifestIndex, manifestSuffix);
    }

    public String nextManifestName() {
        return String.format("/META-INF/manifest%d.%s", ++manifestIndex, manifestSuffix);
    }

    public String nextAnnotationsManifestName() {
        return String.format("/META-INF/annotmanifest%d.%s", ++annotationsManifestIndex, manifestSuffix);
    }

    public String nextSignatureName() {
        return String.format("/META-INF/signature%d.%s", ++signatureIndex, signatureSuffix);
    }

    public String nextAnnotationManifestName() {
        return String.format("/META-INF/annotation%d.%s", ++annotationManifestIndex, signatureSuffix);
    }

    public String nextAnnotationDataFileName() {
        return String.format("/META-INF/annotation%d.dat", ++annotationIndex);
    }
}
