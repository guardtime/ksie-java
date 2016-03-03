package com.guardtime.container.packaging.zip;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZipContainerPackagingFactory implements ContainerPackagingFactory<ZipBlockChainContainer> {

    private static final Logger logger = LoggerFactory.getLogger(ZipContainerPackagingFactory.class);

    private final SignatureFactory signatureFactory;
    private final ContainerManifestFactory manifestFactory;

    public ZipContainerPackagingFactory(SignatureFactory signatureFactory, ContainerManifestFactory manifestFactory) {
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(manifestFactory, "Manifest factory");
        this.signatureFactory = signatureFactory;
        this.manifestFactory = manifestFactory;
        logger.info("Zip container factory initialized");
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
        ContentSigner signer = new ContentSigner(files, annotations);
        SignatureContent signatureContent = signer.sign();
        return new ZipBlockChainContainer(signatureContent);
    }

    @Override
    public ZipBlockChainContainer create(ZipBlockChainContainer existingSignature, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException {
        // TODO implement
        return null;
    }

    class ContentSigner {

        private List<ContainerDocument> documents;
        private List<ContainerAnnotation> annotations;
        private ZipEntryNameProvider nameProvider;

        private List<Pair<String, ContainerAnnotation>> annotationPairs = new LinkedList<>();
        private List<Pair<String, AnnotationInfoManifest>> annotationManifestsPairs = new LinkedList<>();
        private Map<String, Pair<ContainerAnnotation, AnnotationInfoManifest>> annotationsManifestContent = new HashMap<>();

        public ContentSigner(List<ContainerDocument> documents, List<ContainerAnnotation> annotations) {
            this.documents = documents;
            this.annotations = annotations;
        }

        public SignatureContent sign() throws BlockChainContainerException {
            ManifestFactoryType manifestFactoryType = manifestFactory.getManifestFactoryType();
            SignatureFactoryType signatureFactoryType = signatureFactory.getSignatureFactoryType();
            this.nameProvider = new ZipEntryNameProvider(manifestFactoryType.getManifestFileExtension(), signatureFactoryType.getSignatureFileExtension());
            logger.info("'{}' is used to create and read container manifests", manifestFactoryType.getName());
            logger.info("'{}' is used to create and read container signatures", signatureFactoryType.getName());

            Pair<String, DataFilesManifest> dataFilesManifest = Pair.of(nameProvider.nextDataManifestName(), manifestFactory.createDataFilesManifest(documents));
            processAnnotations(dataFilesManifest);
            AnnotationsManifest annotationsManifest = manifestFactory.createAnnotationsManifest(annotationsManifestContent);
            Pair<String, AnnotationsManifest> annotationsManifestPair = Pair.of(nameProvider.nextAnnotationsManifestName(), annotationsManifest);

            SignatureManifest signatureManifest = manifestFactory.createSignatureManifest(dataFilesManifest, annotationsManifestPair,
                    Pair.of(nameProvider.nextSignatureName(), signatureFactoryType.getSignatureMimeType()));

            SignatureContent signatureContent = new SignatureContent.Builder()
                    .withDocuments(documents)
                    .withDataManifest(dataFilesManifest)
                    .withAnnotations(annotationPairs)
                    .withAnnotationManifests(annotationManifestsPairs)
                    .withAnnotationsManifest(annotationsManifestPair)
                    .withManifest(Pair.of(nameProvider.nextManifestName(), signatureManifest))
                    .build();

            DataHash hash = signatureContent.getSignatureInputHash();
            ContainerSignature signature = signatureFactory.create(hash);
            signatureContent.setSignature(signature);
            return signatureContent;
        }

        private void processAnnotations(Pair<String, DataFilesManifest> dataFilesManifest) throws InvalidManifestException {
            if(annotations == null) {
                return;
            }
            for (ContainerAnnotation annotation : annotations) {
                Pair<String, ContainerAnnotation> annotationPair = Pair.of(nameProvider.nextAnnotationDataFileName(), annotation);
                annotationPairs.add(annotationPair);
                AnnotationInfoManifest annotationManifest = manifestFactory.createAnnotationManifest(dataFilesManifest, annotationPair);
                String annotationManifestName = nameProvider.nextAnnotationManifestName();
                annotationManifestsPairs.add(Pair.of(annotationManifestName, annotationManifest));
                annotationsManifestContent.put(annotationManifestName, Pair.of(annotation, annotationManifest));
            }
        }

    }

}
