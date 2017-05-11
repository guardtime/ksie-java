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

package com.guardtime.container;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


public class ContainerBuilderTest extends AbstractContainerTest {

    @Mock
    private ContainerPackagingFactory mockedPackagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedPackagingFactory.create(Mockito.anyList(), Mockito.anyList())).thenReturn(Mockito.mock(Container.class));
    }

    @Test
    public void testCreateBuilder() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        assertNotNull(builder);
    }

    @Test
    public void testCreateBuilderWithoutPackagingFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new ContainerBuilder(null);
    }

    @Test
    public void testAddDocumentToContainer() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        try (StreamContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)) {
            builder.withDocument(document);
            assertEquals(1, builder.getDocuments().size());

            builder.withDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), TEST_FILE_NAME_TEST_DOC, MIME_TYPE_APPLICATION_TXT);
            assertEquals(2, builder.getDocuments().size());

            File mockFile = Mockito.mock(File.class);
            when(mockFile.getName()).thenReturn("SomeName.ext");
            builder.withDocument(mockFile, "application/binary");
            assertEquals(3, builder.getDocuments().size());

            closeAll(builder.getDocuments());
        }
    }

    @Test
    public void testAddAnnotationToContainer() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        builder.withAnnotation(STRING_CONTAINER_ANNOTATION);
        assertEquals(1, builder.getAnnotations().size());
    }

    @Test
    public void testCreateSignature() throws Exception {
        ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_TEXT);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

        builder.withAnnotation(STRING_CONTAINER_ANNOTATION);
        try (Container container = builder.build()) {
            assertNotNull(container);
        }
    }

    @Test
    public void testCreateWithExistingContainer() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(mockedManifestFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                disableInternalVerification().
                build();
        // build initial container
        ContainerBuilder builder = new ContainerBuilder(packagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);
        try (Container container = builder.build()) {

            // add new documents to existing container
            builder.withDocument(TEST_DOCUMENT_HELLO_TEXT);
            builder.withExistingContainer(container);
            try (Container newContainer = builder.build()) {

                assertNotNull(newContainer);
                assertEquals(2, newContainer.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testCreateWithMultipleDocumentsWithSameFileName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expectMessage("Document with name '" + TEST_FILE_NAME_TEST_TXT + "' already exists!");
        expectedException.expect(IllegalArgumentException.class);
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
                ContainerDocument streamContainerDocument = new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        ) {
            ContainerBuilder builder = new ContainerBuilder(mockedPackagingFactory);
            builder.withDocument(document);
            builder.withDocument(streamContainerDocument);
            builder.build();
        }
    }

    @Test
    public void testCreateWithExistingContainerWithMultipleDocumentsWithSameFileName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Multiple documents with same name found!");
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
                ContainerDocument streamContainerDocument = new StreamContainerDocument(new ByteArrayInputStream("ImportantDocument-HAHA".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        ) {
            ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                    withSignatureFactory(mockedSignatureFactory).
                    withManifestFactory(mockedManifestFactory).
                    withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                    disableInternalVerification().
                    build();
            // build initial container
            ContainerBuilder builder = new ContainerBuilder(packagingFactory);
            builder.withDocument(document);
            try (Container container = builder.build()) {

                // add new documents to existing container
                builder.withDocument(streamContainerDocument);
                builder.withExistingContainer(container);
                builder.build();
            }
        }
    }

    @Test
    public void testCreateNewContainerUsingExistingContainerAndExistingDocument() throws Exception {
        ContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(mockedManifestFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                disableInternalVerification().
                build();
        // build initial container
        ContainerBuilder builder = new ContainerBuilder(packagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

        try (Container container = builder.build()) {
            // add new documents to existing container
            builder.withExistingContainer(container);
            builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

            try (Container newContainer = builder.build()) {
                assertNotNull(newContainer);
                assertEquals(2, newContainer.getSignatureContents().size());
                Set<String> documentPaths = new HashSet<>();
                for (SignatureContent content : newContainer.getSignatureContents()) {
                    documentPaths.addAll(content.getDocuments().keySet());
                }
                assertEquals(1, documentPaths.size());
            }
        }
    }

}
