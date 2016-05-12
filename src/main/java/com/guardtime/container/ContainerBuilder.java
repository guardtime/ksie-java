package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.FileContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.util.Util.notNull;

public class ContainerBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerBuilder.class);

    private final List<ContainerDocument> documents = new LinkedList<>();
    private final List<ContainerAnnotation> annotations = new LinkedList<>();

    private final ContainerPackagingFactory packagingFactory;
    private Container existingContainer;

    public ContainerBuilder(ContainerPackagingFactory packagingFactory) {
        notNull(packagingFactory, "Packaging factory");
        this.packagingFactory = packagingFactory;
    }

    public ContainerBuilder withExistingContainer(Container existingContainer) {
        this.existingContainer = existingContainer;
        return this;
    }

    public ContainerBuilder withDocument(InputStream input, String name, String mimeType) {
        return withDocument(new StreamContainerDocument(input, mimeType, name));
    }

    public ContainerBuilder withDocument(File file, String mimeType) {
        return withDocument(new FileContainerDocument(file, mimeType));
    }

    public ContainerBuilder withDocument(ContainerDocument document) {
        notNull(document, "Data file ");
        documents.add(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Document '{}' will be added to the container", document);
        }
        return this;
    }

    public ContainerBuilder withAnnotation(ContainerAnnotation annotation) {
        notNull(annotations, "Annotation");
        annotations.add(annotation);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Annotation '{}' will be added to the container", annotation);
        }
        return this;
    }

    public Container build() throws ContainerException {
        if (existingContainer == null) {
            return packagingFactory.create(documents, annotations);
        } else {
            return packagingFactory.create(existingContainer, documents, annotations);
        }
    }

    List<ContainerDocument> getDocuments() {
        return documents;
    }

    List<ContainerAnnotation> getAnnotations() {
        return annotations;
    }
}
