package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Creates and parses manifests with TLV (Type Length Value) structure.
 */
public class TlvContainerManifestFactory implements ContainerManifestFactory<TlvManifest, TlvDocumentsManifest, TlvAnnotationsManifest, TlvSingleAnnotationManifest> {
    private static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA2_256;
    private static final TlvManifestFactoryType TLV_MANIFEST_FACTORY_TYPE = new TlvManifestFactoryType("TLV manifest factory", "tlv");

    private final HashAlgorithm algorithm;

    public TlvContainerManifestFactory() {
        this(DEFAULT_HASH_ALGORITHM);
    }

    /**
     * @param algorithm
     *         defines what algorithm will be used to generate {@link com.guardtime.ksi.hashing.DataHash} used in {@link
     *         com.guardtime.container.manifest.FileReference}
     */
    public TlvContainerManifestFactory(HashAlgorithm algorithm) {
        Util.notNull(algorithm, "Hash algorithm");
        this.algorithm = algorithm;
    }

    @Override
    public TlvManifest createManifest(Pair<String, TlvDocumentsManifest> documentsManifest, Pair<String, TlvAnnotationsManifest> annotationManifest, Pair<String, String> signatureReference) throws InvalidManifestException {
        Util.notNull(documentsManifest, "Documents manifest");
        Util.notNull(annotationManifest, "Annotations manifest");
        return new TlvManifest(documentsManifest, annotationManifest, signatureReference, algorithm);
    }

    @Override
    public TlvDocumentsManifest createDocumentsManifest(List<ContainerDocument> files) throws InvalidManifestException {
        Util.notNull(files, "Document files list");
        Util.notEmpty(files, "Document files list");
        return new TlvDocumentsManifest(files, algorithm);
    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(Map<String, Pair<ContainerAnnotation, TlvSingleAnnotationManifest>> annotationManifest) throws InvalidManifestException {
        return new TlvAnnotationsManifest(annotationManifest, algorithm);
    }

    @Override
    public TlvSingleAnnotationManifest createSingleAnnotationManifest(Pair<String, TlvDocumentsManifest> documentsManifest, Pair<String, ContainerAnnotation> annotation) throws InvalidManifestException {
        Util.notNull(documentsManifest, "Documents manifest");
        Util.notNull(annotation, "Annotation");
        return new TlvSingleAnnotationManifest(annotation, documentsManifest, algorithm);
    }

    @Override
    public TlvManifest readManifest(InputStream input) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvManifest(input);
    }

    @Override
    public TlvDocumentsManifest readDocumentsManifest(InputStream input) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvDocumentsManifest(input);
    }

    @Override
    public TlvAnnotationsManifest readAnnotationsManifest(InputStream input) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvAnnotationsManifest(input);
    }

    @Override
    public TlvSingleAnnotationManifest readSingleAnnotationManifest(InputStream input) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvSingleAnnotationManifest(input);
    }

    @Override
    public TlvManifestFactoryType getManifestFactoryType() {
        return TLV_MANIFEST_FACTORY_TYPE;
    }

}
