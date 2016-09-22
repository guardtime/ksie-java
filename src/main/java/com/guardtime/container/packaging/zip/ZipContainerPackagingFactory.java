package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Generates {@link Container} instances that use ZIP archiving for storing.
 */
public class ZipContainerPackagingFactory implements ContainerPackagingFactory<ZipContainer> {

    private static final Logger logger = LoggerFactory.getLogger(ZipContainerPackagingFactory.class);

    public static final String MIME_TYPE_ENTRY_NAME = "mimetype";
    private static final String CONTAINER_MIME_TYPE = "application/guardtime.ksie10+zip";

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
    public ZipContainer read(InputStream input) throws InvalidPackageException {
        Util.notNull(input, "Input stream");
        try {
            ZipContainerReader reader = new ZipContainerReader(manifestFactory, signatureFactory);
            return reader.read(input);
        } catch (IOException e) {
            throw new InvalidPackageException("Failed to parse InputStream", e);
        }
    }

    @Override
    public ZipContainer create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException {
        Util.notEmpty(files, "Document files");
        try {
            ContentSigner signer = new ContentSigner(files, annotations);
            ZipSignatureContent signatureContent = signer.sign();
            MimeTypeEntry mimeType = new MimeTypeEntry(MIME_TYPE_ENTRY_NAME, getMimeTypeContent());
            return new ZipContainer(signatureContent, mimeType, signer.getNameProvider());
        } catch (IOException | InvalidManifestException e) {
            throw new InvalidPackageException("Failed to create ZipContainer internal structure!", e);
        } catch (SignatureException e) {
            throw new InvalidPackageException("Failed to sign ZipContainer!", e);
        }
    }

    public byte[] getMimeTypeContent() {
        return CONTAINER_MIME_TYPE.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public ZipContainer create(Container existingContainer, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException {
        Util.notNull(existingContainer, "Container");
        // TODO: Possibility to add signature without adding data files. Spec needed for this
        Util.notEmpty(files, "Data files");
        try {
            ZipContainer existingZipContainer = (ZipContainer) existingContainer;
            ContentSigner signer = new ContentSigner(files, annotations, existingZipContainer.getNameProvider());
            ZipSignatureContent signatureContent = signer.sign();
            existingZipContainer.getSignatureContents().add(signatureContent);
            return existingZipContainer;
        } catch (IOException | InvalidManifestException e) {
            throw new InvalidPackageException("Failed to create ZipContainer internal structure!", e);
        } catch (SignatureException e) {
            throw new InvalidPackageException("Failed to sign ZipContainer!", e);
        }
    }

    class ContentSigner {

        private List<ContainerDocument> documents;
        private List<ContainerAnnotation> annotations;
        private ZipEntryNameProvider nameProvider;

        private List<Pair<String, ContainerAnnotation>> annotationPairs = new LinkedList<>();
        private List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestPairs = new LinkedList<>();
        private Map<String, Pair<ContainerAnnotation, SingleAnnotationManifest>> annotationsManifestContent = new HashMap<>();

        public ContentSigner(List<ContainerDocument> documents, List<ContainerAnnotation> annotations) {
            this.documents = documents;
            this.annotations = annotations;

            ManifestFactoryType manifestFactoryType = manifestFactory.getManifestFactoryType();
            SignatureFactoryType signatureFactoryType = signatureFactory.getSignatureFactoryType();
            this.nameProvider = new ZipEntryNameProvider(manifestFactoryType.getManifestFileExtension(), signatureFactoryType.getSignatureFileExtension());
        }

        public ContentSigner(List<ContainerDocument> documents, List<ContainerAnnotation> annotations, ZipEntryNameProvider nameProvider) {
            this.documents = documents;
            this.annotations = annotations;
            this.nameProvider = nameProvider;
        }

        public ZipSignatureContent sign() throws InvalidManifestException, SignatureException, IOException {
            ManifestFactoryType manifestFactoryType = manifestFactory.getManifestFactoryType();
            SignatureFactoryType signatureFactoryType = signatureFactory.getSignatureFactoryType();
            logger.info("'{}' is used to create and read container manifests", manifestFactoryType.getName());
            logger.info("'{}' is used to create and read container signatures", signatureFactoryType.getName());
            Pair<String, DocumentsManifest> documentsManifest = Pair.of(nameProvider.nextDocumentsManifestName(), manifestFactory.createDocumentsManifest(documents));
            processAnnotations(documentsManifest);
            AnnotationsManifest annotationsManifest = manifestFactory.createAnnotationsManifest(annotationsManifestContent);
            Pair<String, AnnotationsManifest> annotationsManifestPair = Pair.of(nameProvider.nextAnnotationsManifestName(), annotationsManifest);

            Manifest manifest = manifestFactory.createManifest(documentsManifest, annotationsManifestPair,
                    Pair.of(nameProvider.nextSignatureName(), signatureFactoryType.getSignatureMimeType()));

            ZipSignatureContent signatureContent = new ZipSignatureContent.Builder()
                    .withDocuments(documents)
                    .withDocumentsManifest(documentsManifest)
                    .withAnnotations(annotationPairs)
                    .withSingleAnnotationManifests(singleAnnotationManifestPairs)
                    .withAnnotationsManifest(annotationsManifestPair)
                    .withManifest(Pair.of(nameProvider.nextManifestName(), manifest))
                    .build();

            DataHash hash = getSignatureContentSigningHash(signatureContent);
            ContainerSignature signature = signatureFactory.create(hash);
            signatureContent.setSignature(signature);
            return signatureContent;
        }

        private DataHash getSignatureContentSigningHash(ZipSignatureContent signatureContent) throws IOException {
            Manifest manifest = signatureContent.getManifest().getRight();
            HashAlgorithmProvider algorithmProvider = manifestFactory.getHashAlgorithmProvider();
            return manifest.getDataHash(algorithmProvider.getSigningHashAlgorithm());
        }

        public ZipEntryNameProvider getNameProvider() {
            return nameProvider;
        }

        private void processAnnotations(Pair<String, DocumentsManifest> documentsManifest) throws InvalidManifestException {
            if (annotations == null) {
                return;
            }
            for (ContainerAnnotation annotation : annotations) {
                Pair<String, ContainerAnnotation> annotationPair = Pair.of(nameProvider.nextAnnotationDataFileName(), annotation);
                annotationPairs.add(annotationPair);
                SingleAnnotationManifest singleAnnotationManifest = manifestFactory.createSingleAnnotationManifest(documentsManifest, annotationPair);
                String annotationManifestName = nameProvider.nextSingleAnnotationManifestName();
                singleAnnotationManifestPairs.add(Pair.of(annotationManifestName, singleAnnotationManifest));
                annotationsManifestContent.put(annotationManifestName, Pair.of(annotation, singleAnnotationManifest));
            }
        }

    }

}
