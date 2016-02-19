package com.guardtime.container.manifest;


import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ContainerManifestFactory<S extends SignatureManifest, D extends DataFilesManifest, A extends AnnotationsManifest, AI extends AnnotationInfoManifest> {

    S createSignatureManifest(D dataFilesManifest, A annotationManifest, String manifestUri, String signatureURI) throws BlockChainContainerException;

    D createDataFilesManifest(List<ContainerDocument> files, String manifestUri) throws BlockChainContainerException;

    A createAnnotationsManifest(Map<ContainerAnnotation, AI> annotationManifests, String manifestUri) throws BlockChainContainerException;

    AI createAnnotationManifest(D dataManifest, ContainerAnnotation annotation, String manifestUri) throws BlockChainContainerException;

    S readSignatureManifest(InputStream input, String manifestUri) throws BlockChainContainerException;

    D readDataFilesManifest(InputStream input, String manifestUri) throws BlockChainContainerException;

    A readAnnotationsManifest(InputStream input, String manifestUri) throws BlockChainContainerException;

    AI readAnnotationManifest(InputStream input, String manifestUri) throws BlockChainContainerException;

}
