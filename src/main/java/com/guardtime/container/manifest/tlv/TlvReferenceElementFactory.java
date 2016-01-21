package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.TlvTypes;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVHeader;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class TlvReferenceElementFactory {


    public static TLVElement createDataManifestReferenceTlvElement(DataFilesManifest dataManifest) throws BlockChainContainerException {
        try {
            TLVElement element = new TLVElement(new TLVHeader(false, false, TlvTypes.DATA_FILES_MANIFEST_REFERENCE.getType()));

            element.addChildElement(createUriElement(dataManifest.getUri())); // TODO: Problematic as this should be full URI and this might depend on the packaging format :S
            element.addChildElement(createHashElement(Util.hash(dataManifest.getInputStream(), HashAlgorithm.SHA2_256))); // TODO: Umn, don't like having the algorithm here

            TLVElement mimeType = new TLVElement(new TLVHeader(false, false, 0x3));
            mimeType.setStringContent("ksie10/datamanifest");
            element.addChildElement(mimeType);

            return element;
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createAnnotationReferenceTlvElement(ContainerAnnotation annotation) {
        try {
            TLVElement element = new TLVElement(new TLVHeader(false, false, TlvTypes.ANNOTATION_REFERENCE.getType()));

            element.addChildElement(createUriElement(annotation.getUri()));// TODO: Problematic as this should be full URI and this might depend on the packaging format :S
            element.addChildElement(createHashElement(annotation.getDataHash(HashAlgorithm.SHA2_256))); // TODO: Umn, don't like having the hash algorithm here

            TLVElement domain = new TLVElement(new TLVHeader(false, false, 0x3));
            domain.setStringContent(annotation.getDomain());
            element.addChildElement(domain);

            return element;
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createAnnotationInfoReferenceTlvElement(TlvAnnotationInfoManifest annotationInfo) {
        try {
            TLVElement element = new TLVElement(new TLVHeader(false, false, TlvTypes.ANNOTATION_INFO_REFERENCE.getType()));

            element.addChildElement(createUriElement(annotationInfo.getUri()));// TODO: Problematic as this should be full URI and this might depend on the packaging format :S
            element.addChildElement(createHashElement(Util.hash(annotationInfo.getInputStream(), HashAlgorithm.SHA2_256))); // TODO: Umn, don't like having the hash algorithm here

            TLVElement type = new TLVElement(new TLVHeader(false, false, 0x3));
            type.setStringContent(annotationInfo.getAnnotation().getMimeType());
            element.addChildElement(type);

            return element;
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createDocumentReferenceTlvElement(ContainerDocument document) {
        try {
            TLVElement element = new TLVElement(new TLVHeader(false, false, TlvTypes.DATA_FILE_REFERENCE.getType()));

            element.addChildElement(createUriElement(document.getFileName()));// TODO: Problematic as this should be full URI and this might depend on the packaging format :S
            element.addChildElement(createHashElement(document.getDataHash(HashAlgorithm.SHA2_256))); // TODO: Umn, don't like having the hash algorithm here

            TLVElement mime = new TLVElement(new TLVHeader(false, false, 0x3));
            mime.setStringContent(document.getMimeType());
            element.addChildElement(mime);

            return element;
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    private static TLVElement createUriElement(String uri) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, 0x1));
        element.setStringContent(uri);
        return element;
    }

    private static TLVElement createHashElement(DataHash hash) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, 0x2));
        element.setDataHashContent(hash);
        return element;
    }

    public static TLVElement createAnnotationsManifestReferenceTlvElement(TlvAnnotationsManifest annotationsManifest) {
        try {
            TLVElement element = new TLVElement(new TLVHeader(false, false, TlvTypes.ANNOTATIONS_MANIFEST_REFERENCE.getType()));

            element.addChildElement(createUriElement(annotationsManifest.getUri()));// TODO: Problematic as this should be full URI and this might depend on the packaging format :S
            element.addChildElement(createHashElement(Util.hash(annotationsManifest.getInputStream(), HashAlgorithm.SHA2_256))); // TODO: Umn, don't like having the hash algorithm here

            TLVElement mime = new TLVElement(new TLVHeader(false, false, 0x3));
            mime.setStringContent("ksie10/annotmanifest");
            element.addChildElement(mime);

            return element;
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public static TLVElement createSignatureReferenceTlvElement() { // TODO: Really need to pass in something here, probably container or datamanifest or annotationsmanifest or sth to get the signature index
        try {
            TLVElement element = new TLVElement(new TLVHeader(false, false, TlvTypes.SIGNATURE_REFERENCE.getType()));

            element.addChildElement(createUriElement("META-INF/signature1.ksig"));// TODO: Problematic as this should be full URI and this might depend on the packaging format :S

            TLVElement mime = new TLVElement(new TLVHeader(false, false, 0x3));
            mime.setStringContent("application/ksi-signature");
            element.addChildElement(mime);

            return element;
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }
}
