package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TlvContainerManifestFactory implements ContainerManifestFactory<TlvSignatureManifest, TlvDataFilesManifest, TlvAnnotationsManifest, TlvAnnotationInfoManifest> {

    private final String TLV_EXTENSION = ".tlv";

    @Override
    public TlvSignatureManifest createSignatureManifest(TlvDataFilesManifest dataFilesManifest, TlvAnnotationsManifest annotationsManifest, String manifestUri) throws BlockChainContainerException {
        Util.notNull(dataFilesManifest, "Document manifest");
        Util.notNull(annotationsManifest, "Annotations manifest");
        List<TLVElement> elements = new LinkedList<>();
        try {
            elements.add(TlvReferenceElementFactory.createDataManifestReferenceTlvElement(dataFilesManifest));
            elements.add(TlvReferenceElementFactory.createSignatureReferenceTlvElement("META-INF/signature1.ksig")); // TODO: Find a solution to generate correct signature path
            elements.add(TlvReferenceElementFactory.createAnnotationsManifestReferenceTlvElement(annotationsManifest));
            return new TlvSignatureManifest(elements, manifestUri + TLV_EXTENSION);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public TlvDataFilesManifest createDataFilesManifest(List<ContainerDocument> files, String manifestUri) throws BlockChainContainerException {
        Util.notEmpty(files, "Document files list");
        List<TLVElement> elements = new LinkedList<>();
        try {
            for (ContainerDocument doc : files) {
                elements.add(TlvReferenceElementFactory.createDocumentReferenceTlvElement(doc));
            }
            return new TlvDataFilesManifest(elements, manifestUri + TLV_EXTENSION);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }

    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(Map<ContainerAnnotationType, TlvAnnotationInfoManifest> annotationManifests, String manifestUri) throws BlockChainContainerException {
        if (annotationManifests.isEmpty()) {
            throw new IllegalArgumentException("Annotation info manifests list");
        }
        List<TLVElement> elements = new LinkedList<>();
        TlvAnnotationInfoManifest manifest;
        try {
            for (ContainerAnnotationType annotationType : annotationManifests.keySet()) {
                manifest = annotationManifests.get(annotationType);
                elements.add(TlvReferenceElementFactory.createAnnotationInfoReferenceTlvElement(manifest, annotationType));
            }

            return new TlvAnnotationsManifest(elements, manifestUri + TLV_EXTENSION);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public TlvAnnotationInfoManifest createAnnotationManifest(TlvDataFilesManifest dataManifest, ContainerAnnotation annotation, String manifestUri) throws BlockChainContainerException {
        Util.notNull(dataManifest, "Document manifest");
        Util.notNull(annotation, "Annotation");
        try {
            List<TLVElement> elements = new LinkedList<>();
            elements.add(TlvReferenceElementFactory.createDataManifestReferenceTlvElement(dataManifest));
            elements.add(TlvReferenceElementFactory.createAnnotationReferenceTlvElement(annotation));

            return new TlvAnnotationInfoManifest(elements, manifestUri + TLV_EXTENSION);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public TlvSignatureManifest readSignatureManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        try {
            return new TlvSignatureManifest(input, manifestUri);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public TlvDataFilesManifest readDataFilesManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        try {
            return new TlvDataFilesManifest(input, manifestUri);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public TlvAnnotationsManifest readAnnotationsManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        try {
            return new TlvAnnotationsManifest(input, manifestUri);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public TlvAnnotationInfoManifest readAnnotationManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        try {
            return new TlvAnnotationInfoManifest(input, manifestUri);
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }
}
