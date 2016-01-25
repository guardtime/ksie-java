package com.guardtime.container.manifest;


import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.InputStream;
import java.util.List;

public interface ContainerManifestFactory<S extends SignatureManifest, D extends DataFilesManifest, A extends AnnotationsManifest, AI extends AnnotationInfoManifest> {

    S createSignatureManifest(D dataFilesManifest, A annotationManifest, String manifestUri) throws BlockChainContainerException;

    D createDataFilesManifest(List<ContainerDocument> files, String manifestUri) throws BlockChainContainerException;

    A createAnnotationsManifest(List<AI> annotationManifests, String manifestUri) throws BlockChainContainerException;

    AI createAnnotationManifest(D dataManifest, ContainerAnnotation annotation) throws BlockChainContainerException;

    S readSignatureManifest(InputStream input);

    D readDataFilesManifest(InputStream input);

    A readAnnotationsManifest(InputStream input);

    AI readAnnotationManifest(InputStream input);

}
