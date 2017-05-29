/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.exception.InvalidPackageException;

import java.io.InputStream;
import java.util.List;

/**
 * Creates or parses {@link Container} instances.
 * @param <C>
 */
public interface ContainerPackagingFactory<C extends Container> {
    /**
     * Parses an {@link InputStream} to produce a {@link Container}.
     *
     * @param input    An {@link InputStream} that contains a valid/parsable {@link Container}. This InputStream will be closed
     *                 after reading.
     * @return An instance of {@link Container} based on the data from {@link InputStream}. Does not verify
     *         the container/signature(s).
     * @throws InvalidPackageException      When the {@link InputStream} does not contain a parsable {@link Container}.
     * @throws ContainerReadingException    When there were issues parsing some elements of the {@link Container}. The parsed
     *         container and all encountered exceptions can be retrieved from this exception.
     */
    C read(InputStream input) throws InvalidPackageException;

    /**
     * Creates a {@link Container} with the input documents and annotations and a signature covering them.
     *
     * @param files          List of {@link ContainerDocument} to be added and signed. Can NOT be null.
     * @param annotations    List of {@link ContainerAnnotation} to be added and signed. Can be null.
     * @return A new {@link Container} which contains the documents and annotations and a signature covering them.
     * @throws InvalidPackageException  When the input data can not be processed or signing fails.
     */
    C create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException;

    /**
     * Creates a {@link Container} that combines everything from the existing {@link Container} and the new set of
     * documents, annotations and a signature for the added elements.
     *
     * @param existingContainer    An instance of {@link Container} which already has
     *                             {@link com.guardtime.container.signature.ContainerSignature}(s)
     * @param files                List of {@link ContainerDocument} to be added and signed. Can NOT be null.
     * @param annotations          List of {@link ContainerAnnotation} to be added and signed. Can be null.
     * @return A new {@link Container} which contains everything from existingContainer and the added documents
     * and annotations and a signature to cover them.
     * @throws InvalidPackageException When the input data can not be processed or signing fails.
     */
    C create(Container existingContainer, List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException;

    /**
     * Provides the MIMETYPE content for container.
     */
    byte[] getMimeTypeContent();
}
