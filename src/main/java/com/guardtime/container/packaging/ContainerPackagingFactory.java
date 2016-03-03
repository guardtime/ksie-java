package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ContainerPackagingFactory<C extends BlockChainContainer> {

    C read(InputStream input) throws IOException;

    C create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException;

    C create(C existingSignature, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException;

}
