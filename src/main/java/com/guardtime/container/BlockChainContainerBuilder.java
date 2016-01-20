package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.FileContainerDocument;
import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.BlockChainContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.util.Util.notNull;

public class BlockChainContainerBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainContainerBuilder.class);

    private List<ContainerDocument> documents = new LinkedList<>();
    private List<ContainerAnnotation> annotations = new LinkedList<>();

    private SignatureFactory signatureFactory;
    private ContainerManifestFactory manifestFactory;
    private BlockChainContainerPackagingFactory packagingFactory;

    public BlockChainContainerBuilder(SignatureFactory signatureFactory, ContainerManifestFactory manifestFactory, BlockChainContainerPackagingFactory packagingFactory) {
        notNull(signatureFactory, "Signature factory");
        notNull(manifestFactory, "Manifest factory");
        notNull(packagingFactory, "Packaging factory");
        this.signatureFactory = signatureFactory;
        this.manifestFactory = manifestFactory;
        this.packagingFactory = packagingFactory;
    }

    public BlockChainContainerBuilder withDataFile(InputStream input, String name, String mimeType) {
        return withDataFile(new StreamContainerDocument(input, name, mimeType));
    }

    public BlockChainContainerBuilder withDataFile(File file, String mimeType) {
        return withDataFile(new FileContainerDocument(file, mimeType));
    }

    public BlockChainContainerBuilder withDataFile(ContainerDocument document) {
        notNull(document, "Data file ");
        documents.add(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Document {} will be added to the container");
        }
        return this;
    }

    public BlockChainContainerBuilder withAnnotation(ContainerAnnotation annotation) {
        notNull(annotations, "Annotation");
        annotations.add(annotation);
        return this;
    }

    public BlockChainContainer build() {
        return null;
    }

    List<ContainerDocument> getDocuments() {
        return documents;
    }

    List<ContainerAnnotation> getAnnotations() {
        return annotations;
    }
}
