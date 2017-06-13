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

package com.guardtime.container.packaging.zip;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.UuidIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.exception.ContainerMergingException;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ZipContainerTest extends AbstractContainerTest {

    @Test
    public void testAddSingleSignatureContent_OK() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<ContainerAnnotation>())) {
                container.add(newContainer.getSignatureContents().get(0));
                assertEquals(2, container.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddContainer_OK() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<ContainerAnnotation>())) {
                container.add(newContainer);
                assertEquals(2, container.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddListOfSignatureContent_OK() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<ContainerAnnotation>());
                 ByteArrayInputStream input = new ByteArrayInputStream("auh".getBytes(StandardCharsets.UTF_8));
                 ContainerDocument containerDocument = new StreamContainerDocument(input, "text/plain", "someTestFile.txt")
            ) {
                packagingFactory.addSignature(newContainer, singletonList(containerDocument), new ArrayList<ContainerAnnotation>());
                int expected = newContainer.getSignatureContents().size() + 1;
                container.add(newContainer);
                assertEquals(expected, container.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddWithSameManifestPath_ThrowsContainerMergingException() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Manifest!");
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_CONTAINER_ANNOTATION))) {
            assertEquals(1, container.getSignatureContents().size());
            try (Container newContainer = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<ContainerAnnotation>())) {
                container.add(newContainer.getSignatureContents().get(0));
            }
        }
    }

    @Test
    public void testAddWithSameContainerDocumentPath_ThrowsContainerMergingException() throws Exception {
        expectedException.expect(ContainerMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for ContainerDocument! Path: ");
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Container container = packagingFactory.create(
                singletonList(TEST_DOCUMENT_HELLO_PDF),
                singletonList(STRING_CONTAINER_ANNOTATION)
        )) {
            assertEquals(1, container.getSignatureContents().size());
            try (ContainerDocument clashingDocument = new StreamContainerDocument(
                    new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                    TEST_DOCUMENT_HELLO_PDF.getMimeType(),
                    TEST_DOCUMENT_HELLO_PDF.getFileName()
            );
                 Container newContainer = packagingFactory.create(singletonList(clashingDocument), new ArrayList<ContainerAnnotation>())
            ) {
                container.add(newContainer.getSignatureContents().get(0));
            }
        }
    }
}
