package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.InputStream;
import java.util.List;

public interface ContainerPackagingFactory<C extends BlockChainContainer> {

    C read(InputStream input) throws InvalidPackageException;

    C create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException;

    C create(C existingSignature, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException;

    byte[] getMimeTypeContent();
}
