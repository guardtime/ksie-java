package com.guardtime.container.packaging.zip;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.BlockChainContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZipContainerPackagingFactory implements BlockChainContainerPackagingFactory<ZipBlockChainContainer> {

    private static final String META_INF_DIR_NAME = "/META-INF/";

    private final SignatureFactory signatureFactory;
    private final ContainerManifestFactory manifestFactory;

    public ZipContainerPackagingFactory(SignatureFactory signatureFactory, ContainerManifestFactory manifestFactory) {
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(manifestFactory, "Manifest factory");
        this.signatureFactory = signatureFactory;
        this.manifestFactory = manifestFactory;
    }

    @Override
    public ZipBlockChainContainer read(InputStream input) {

        return null;
    }

    @Override
    public ZipBlockChainContainer create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException {
        Util.notEmpty(files, "Data files");
        DataFilesManifest dataFilesManifest = manifestFactory.createDataFilesManifest(files, META_INF_DIR_NAME + "datamanifest");
        Map<ContainerAnnotationType, AnnotationInfoManifest> annotationInfoManifests = createAnnotationInfoManifests(annotations, dataFilesManifest);
        AnnotationsManifest annotationsManifest = manifestFactory.createAnnotationsManifest(annotationInfoManifests, META_INF_DIR_NAME + "annotmanifest");
        SignatureManifest signatureManifest = manifestFactory.createSignatureManifest(dataFilesManifest, annotationsManifest, META_INF_DIR_NAME + "manifest");

        ZipBlockChainContainer container = new Builder(files, annotations).
                withDataFilesManifest(dataFilesManifest).
                withAnnotationInfoManifests(new LinkedList<>(annotationInfoManifests.values())).
                withAnnotationsManifest(annotationsManifest).
                withSignatureManifest(signatureManifest).
                build();

        DataHash hash = container.getSignatureInputHash();
        ContainerSignature signature = signatureFactory.create(hash);
        container.addSignature(signature);
        return container;
    }

    private Map<ContainerAnnotationType, AnnotationInfoManifest> createAnnotationInfoManifests(List<ContainerAnnotation> annotations, DataFilesManifest dataFilesManifest) throws BlockChainContainerException {
        Map<ContainerAnnotationType, AnnotationInfoManifest> annotationInfoManifests = new HashMap<>();
        for (ContainerAnnotation annotation : annotations) {
            AnnotationInfoManifest singleAnnotationManifest = manifestFactory.createAnnotationManifest(dataFilesManifest, annotation, annotation.getUri());
            annotationInfoManifests.put(annotation.getAnnotationType(), singleAnnotationManifest);
        }
        return annotationInfoManifests;
    }

}
