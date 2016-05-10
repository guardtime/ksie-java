package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Creates and parses manifests with TLV (Type Length Value) structure.
 */
public class TlvContainerManifestFactory implements ContainerManifestFactory<TlvManifest, TlvDataFilesManifest, TlvAnnotationsManifest, TlvSingleAnnotationManifest> {

    private static final TlvManifestFactoryType TLV_MANIFEST_FACTORY_TYPE = new TlvManifestFactoryType("TLV manifest factory", "tlv");

    @Override
    public TlvManifest createManifest(Pair<String, TlvDataFilesManifest> dataFilesManifest, Pair<String, TlvAnnotationsManifest> annotationManifest, Pair<String, String> signatureReference) throws InvalidManifestException {
        Util.notNull(dataFilesManifest, "Document manifest");
        Util.notNull(annotationManifest, "Annotations manifest");
        return new TlvManifest(dataFilesManifest, annotationManifest, signatureReference);
    }

    @Override
    public TlvDataFilesManifest createDataFilesManifest(List<ContainerDocument> files) throws InvalidManifestException {
        Util.notNull(files, "Document list");
        Util.notEmpty(files, "Document files list");
        return new TlvDataFilesManifest(files);
    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(Map<String, Pair<ContainerAnnotation, TlvSingleAnnotationManifest>> annotationManifest) throws InvalidManifestException {
        return new TlvAnnotationsManifest(annotationManifest);
    }

    @Override
    public TlvSingleAnnotationManifest createSingleAnnotationManifest(Pair<String, TlvDataFilesManifest> dataManifest, Pair<String, ContainerAnnotation> annotation) throws InvalidManifestException {
        Util.notNull(dataManifest, "Document manifest");
        Util.notNull(annotation, "Annotation");
        return new TlvSingleAnnotationManifest(annotation, dataManifest);
    }

    @Override
    public TlvManifest readManifest(InputStream input) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvManifest(input);
    }

    @Override
    public TlvDataFilesManifest readDataFilesManifest(InputStream input) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvDataFilesManifest(input);
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
