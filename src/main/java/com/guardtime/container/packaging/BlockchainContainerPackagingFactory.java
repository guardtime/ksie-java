package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDataFile;

import java.io.InputStream;
import java.util.List;

public interface BlockchainContainerPackagingFactory<C extends BlockchainContainer> {

    C read(InputStream input);

    C create(List<ContainerDataFile> files, List<ContainerAnnotation> annotations) throws BlockChainContainerException;

}
