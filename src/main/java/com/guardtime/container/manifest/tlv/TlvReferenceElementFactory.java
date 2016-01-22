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

public class TlvReferenceElementFactory {


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

    public static TLVElement createAnnotationReferenceTlvElement(ContainerAnnotation annotation) {
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

    public static TLVElement createAnnotationInfoReferenceTlvElement(TlvAnnotationInfoManifest annotationInfo) {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.ANNOTATION_INFO_REFERENCE).
                    withUriElement(annotationInfo.getUri()).
                    withHashElement(Util.hash(annotationInfo.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                    withMimeTypeElement(annotationInfo.getAnnotation().getMimeType()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createDocumentReferenceTlvElement(ContainerDocument document) {
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

    public static TLVElement createAnnotationsManifestReferenceTlvElement(TlvAnnotationsManifest annotationsManifest) {
        try {
            return new TlvReferenceBuilder().
                    withType(TlvTypes.ANNOTATIONS_MANIFEST_REFERENCE).
                    withUriElement(annotationsManifest.getUri()). // TODO: Problematic as this should be full URI and this might depend on the packaging format :S
                    withHashElement(Util.hash(annotationsManifest.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                    withMimeTypeElement(ContainerManifestMimeType.ANNOTATIONS_MANIFEST.getType()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createSignatureReferenceTlvElement() { // TODO: Really need to pass in something here, probably container or datamanifest or annotationsmanifest or sth to get the signature index
        try {

            return new TlvReferenceBuilder().
                    withType(TlvTypes.SIGNATURE_REFERENCE).
                    withUriElement("META-INF/signature1.ksig"). // TODO: Problematic as this should be full URI and this might depend on the packaging format :S
                    withMimeTypeElement(ContainerManifestMimeType.SIGNATURE_MANIFEST.getType()).
                    build();
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }
}
