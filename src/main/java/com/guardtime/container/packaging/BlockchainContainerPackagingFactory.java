package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.InputStream;
import java.util.List;

public interface BlockChainContainerPackagingFactory<C extends BlockChainContainer> {

    C read(InputStream input);

    C create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException;

}
