package com.guardtime.container.packaging.zip;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ZipContainerPackagingFactory implements BlockChainContainerPackagingFactory<ZipBlockChainContainer> {

    private final SignatureFactory signatureFactory;
    private final ContainerManifestFactory manifestFactory;

    public ZipContainerPackagingFactory(SignatureFactory signatureFactory, ContainerManifestFactory manifestFactory) {
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(manifestFactory, "Manifest factory");
        this.signatureFactory = signatureFactory;
        this.manifestFactory = manifestFactory;
    }

    @Override
    public ZipBlockChainContainer read(InputStream input) throws IOException {
        Util.notNull(input, "Input stream");
        ZipContainerReader reader = new ZipContainerReader(manifestFactory, signatureFactory);
        return reader.read(input);
    }

    @Override
    public ZipBlockChainContainer create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException {
        Util.notEmpty(files, "Data files");
        ZipEntryNameProvider nameProvider = new ZipEntryNameProvider("tlv", "ksi");//TODO get from factories

        Pair<String, DataFilesManifest> dataFilesManifest = Pair.of(nameProvider.nextDataManifestName(), manifestFactory.createDataFilesManifest(files));
        List<Pair<String, AnnotationInfoManifest>> annotationManifests = createAnnotationManifests(dataFilesManifest, annotations, nameProvider);

        Pair<String, AnnotationsManifest> annotationsManifest = Pair.of(nameProvider.nextAnnotationsName(), manifestFactory.createAnnotationsManifest(new HashMap<ContainerAnnotation, Pair>()));
        //TODO signature type
        SignatureManifest signatureManifest = manifestFactory.createSignatureManifest(dataFilesManifest, annotationsManifest, Pair.of(nameProvider.nextSignatureName(), "KSI"));

        SignatureContent signatureContent = new SignatureContent.Builder()
                .withDocuments(files)
                .withDataManifest(dataFilesManifest)
                .withAnnotations(createAnnotationPairs(annotations, nameProvider))
                .withAnnotationManifests(annotationManifests)
                .withAnnotationsManifest(annotationsManifest)
                .withManifest(Pair.of(nameProvider.nextManifestName(), signatureManifest))
                .build();

        DataHash hash = signatureContent.getSignatureInputHash();
        ContainerSignature signature = signatureFactory.create(hash);
        signatureContent.setSignature(signature);

        return new ZipBlockChainContainer(signatureContent);
    }

    @Override
    public ZipBlockChainContainer create(ZipBlockChainContainer existingSignature, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException {
        // TODO implement
        return null;
    }

    private List<Pair<String, AnnotationInfoManifest>> createAnnotationManifests(Pair<String, DataFilesManifest> dataFilesManifest, List<ContainerAnnotation> annotations, ZipEntryNameProvider nameProvider) throws InvalidManifestException {
        List<Pair<String, AnnotationInfoManifest>> annotationManifests = new LinkedList<>();
        for (ContainerAnnotation annotation : annotations) {
            Pair<String, ContainerAnnotation> annotationPair = Pair.of(nameProvider.nextAnnotationDataFileName(), annotation);
            AnnotationInfoManifest annotationManifest = manifestFactory.createAnnotationManifest(dataFilesManifest, annotationPair);
            annotationManifests.add(Pair.of(nameProvider.nextAnnotationManifestName(), annotationManifest));
        }
        return annotationManifests;
    }

    private List<Pair<String, ContainerAnnotation>> createAnnotationPairs(List<ContainerAnnotation> annotations, ZipEntryNameProvider nameProvider) {
        List<Pair<String, ContainerAnnotation>> pairs = new LinkedList<>();
        for (ContainerAnnotation annotation : annotations) {
            pairs.add(Pair.of(nameProvider.nextAnnotationDataFileName(), annotation));
        }
        return pairs;
    }

}
