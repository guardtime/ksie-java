package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.InputStream;
import java.util.List;

public interface ContainerPackagingFactory<C extends BlockChainContainer> {
    /**
     * Parses an {@link InputStream} to produce a {@link BlockChainContainer}.
     *
     * @param input
     *         An {@link InputStream} that contains a valid/parsable {@link BlockChainContainer}.
     * @return An instance of {@link BlockChainContainer} based on the data from {@link InputStream}. Does not verify
     * the container/signature(s).
     * @throws InvalidPackageException
     *         When the {@link InputStream} does not contain a parsable {@link BlockChainContainer}.
     */
    C read(InputStream input) throws InvalidPackageException;

    /**
     * Creates a {@link BlockChainContainer} with the input documents and annotations and a signature covering them.
     *
     * @param files
     *         List of {@link ContainerDocument} to be added and signed. Can NOT be null.
     * @param annotations
     *         List of {@link ContainerAnnotation} to be added and signed. Can be null.
     * @return A new {@link BlockChainContainer} which contains the documents and annotations and a signature covering
     * them.
     * @throws InvalidPackageException
     *         When the input data can not be processed or signing fails.
     */
    C create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException;

    /**
     * Updates an existing {@link BlockChainContainer} to include a new set of documents, annotations and a signature
     * for the added elements.
     *
     * @param existingSignature
     *         An instance of {@link BlockChainContainer} which already has {@link com.guardtime.container.signature.ContainerSignature}(s)
     * @param files
     *         List of {@link ContainerDocument} to be added and signed. Can NOT be null.
     * @param annotations
     *         List of {@link ContainerAnnotation} to be added and signed. Can be null.
     * @return Updated {@link BlockChainContainer} which now contains the added documents and annotations and a
     * signature to cover them.
     * @throws InvalidPackageException
     *         When the input data can not be processed or signing fails.
     */
    C create(C existingSignature, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException;

    byte[] getMimeTypeContent();
}
