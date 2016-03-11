package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.FileContainerDocument;
import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.InvalidPackageException;
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

    private final List<ContainerDocument> documents = new LinkedList<>();
    private final List<ContainerAnnotation> annotations = new LinkedList<>();
    
    private final ContainerPackagingFactory packagingFactory;
    private BlockChainContainer existingContainer;

    public BlockChainContainerBuilder(ContainerPackagingFactory packagingFactory) {
        notNull(packagingFactory, "Packaging factory");
        this.packagingFactory = packagingFactory;
    }

    public BlockChainContainerBuilder withExistingContainer(InputStream input) throws InvalidPackageException {
        return withExistingContainer(packagingFactory.read(input));
    }

    public BlockChainContainerBuilder withExistingContainer(BlockChainContainer existingContainer) {
        this.existingContainer = existingContainer;
        return this;
    }

    public BlockChainContainerBuilder withDataFile(InputStream input, String name, String mimeType) {
        return withDataFile(new StreamContainerDocument(input, mimeType, name));
    }

    public BlockChainContainerBuilder withDataFile(File file, String mimeType) {
        return withDataFile(new FileContainerDocument(file, mimeType));
    }

    public BlockChainContainerBuilder withDataFile(ContainerDocument document) {
        notNull(document, "Data file ");
        documents.add(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Document '{}' will be added to the container", document);
        }
        return this;
    }

    public BlockChainContainerBuilder withAnnotation(ContainerAnnotation annotation) {
        notNull(annotations, "Annotation");
        annotations.add(annotation);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Annotation '{}' will be added to the container", annotation);
        }
        return this;
    }

    public BlockChainContainer build() throws BlockChainContainerException {
        return packagingFactory.create(documents, annotations);
    }

    List<ContainerDocument> getDocuments() {
        return documents;
    }

    List<ContainerAnnotation> getAnnotations() {
        return annotations;
    }
}
