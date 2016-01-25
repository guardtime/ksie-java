package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvReferenceElementFactory {


    private static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA2_256;

    public static TLVElement createDataManifestReferenceTlvElement(DataFilesManifest dataManifest) throws BlockChainContainerException {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.DATA_FILES_MANIFEST_REFERENCE).
                    withUriElement(dataManifest.getUri()).
                    withHashElement(Util.hash(dataManifest.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                    withMimeTypeElement(ContainerManifestMimeType.DATA_MANIFEST.getType()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createAnnotationReferenceTlvElement(ContainerAnnotation annotation) throws BlockChainContainerException {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.ANNOTATION_REFERENCE).
                    withUriElement(annotation.getUri()).
                    withHashElement(annotation.getDataHash(DEFAULT_HASH_ALGORITHM)).
                    withDomainElement(annotation.getDomain()).
                    build();
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createAnnotationInfoReferenceTlvElement(TlvAnnotationInfoManifest annotationInfo) throws BlockChainContainerException {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.ANNOTATION_INFO_REFERENCE).
                    withUriElement(annotationInfo.getUri()).
                    withHashElement(Util.hash(annotationInfo.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                    withMimeTypeElement(annotationInfo.getAnnotation().getAnnotationType().getContent()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createDocumentReferenceTlvElement(ContainerDocument document) throws BlockChainContainerException {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.DATA_FILE_REFERENCE).
                    withUriElement(document.getFileName()).
                    withHashElement(document.getDataHash(DEFAULT_HASH_ALGORITHM)).
                    withMimeTypeElement(document.getMimeType()).
                    build();
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createAnnotationsManifestReferenceTlvElement(TlvAnnotationsManifest annotationsManifest) throws BlockChainContainerException {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.ANNOTATIONS_MANIFEST_REFERENCE).
                    withUriElement(annotationsManifest.getUri()).
                    withHashElement(Util.hash(annotationsManifest.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                    withMimeTypeElement(ContainerManifestMimeType.ANNOTATIONS_MANIFEST.getType()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createSignatureReferenceTlvElement() throws BlockChainContainerException {
        try {

            return new TlvReferenceBuilder().
                    withType(TlvTypes.SIGNATURE_REFERENCE).
                    withUriElement("META-INF/signature1.ksig"). // TODO: Find a solution to generate correct signature path
                    withMimeTypeElement(ContainerManifestMimeType.SIGNATURE_MANIFEST.getType()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }
}
