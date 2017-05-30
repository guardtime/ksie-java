package com.guardtime.container.integration;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.exception.InvalidPackageException;
import com.guardtime.container.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContainerCloseableIntegrationTest extends AbstractCommonIntegrationTest {
    private Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

    @Before
    public void setUp() throws Exception {
        super.setUp();
        closeAll(containerElements);
        cleanUpTempDir();
    }

    private void cleanUpTempDir() throws Exception {
        cleanTempDir();
        assertFalse("Unclean test system! There are some 'ksie' files in " + tmpDir, anyKsieTempFiles());
    }

    @After
    public void assertCleanTempDir() {
        assertFalse("Close did not delete all temporary files!", anyKsieTempFiles());
    }

    @Test
    public void testClosingClosedContainerDoesNotThrowException() throws Exception {
        Container container = getContainer();
        container.close();
        container.close();
    }

    @Test
    public void testCloseDeletesTemporaryFiles() throws Exception {
        Container container = getContainer();
        assertTrue("Temporary files not found!", anyKsieTempFiles());
        container.close();
    }

    @Test
    public void testContainerWithTryWithResources() throws Exception {
        try (Container container = getContainer()) {
            assertFalse(container.getSignatureContents().isEmpty());
            assertTrue("Temporary files not found!", anyKsieTempFiles());
        }
    }

    @Test
    public void testDeleteAllTempFilesAndThenCloseContainer() throws Exception {
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        cleanTempDir();
        container.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testDeleteAllTempFilesFromExistingContainer() throws Exception {
        Container existingContainer = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        List<File> ksieTempFiles = getKsieTempFiles();
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(new byte[313]), "byte inputstream", "byte-input-stream.bis");
                ContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "content", "domain.com")
        ) {
            packagingFactory.addSignature(existingContainer, Collections.singletonList(document), Collections.singletonList(annotation));
            for (File doc : ksieTempFiles) {
                if (isTempFile(doc)) {
                    Util.deleteFileOrDirectory(doc.toPath());
                }
            }
            existingContainer.close();
            assertFalse(anyKsieTempFiles());
        } finally {
            existingContainer.close();
        }
    }

    @Test
    public void testDeleteAllTempFilesFromCreatedContainer() throws Exception {
        Container existingContainer = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        List<File> ksieTempFiles = getKsieTempFiles();
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(new byte[313]), "byte inputstream", "byte-input-stream.bis");
                ContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "content", "domain.com");
        ) {
            packagingFactory.addSignature(existingContainer, Collections.singletonList(document), Collections.singletonList(annotation));
            for (File doc : getKsieTempFiles()) {
                if (isTempFile(doc) && !ksieTempFiles.contains(doc)) {
                    Util.deleteFileOrDirectory(doc.toPath());
                }
            }
            assertTrue(ksieTempFiles.equals(getKsieTempFiles()));
        } finally {
            existingContainer.close();
        }
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testWriteClosedContainer() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Can't write closed object!");
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        container.close();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            container.writeTo(bos);
        }
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testWriteContainerWithTempFileRemoved() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Store has been corrupted! Expected to find file");
        try (
                Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            List<File> tempFiles = getKsieTempFiles();
            assertTrue(tempFiles.size() == container.getSignatureContents().size() + 1);
            Util.deleteFileOrDirectory(tempFiles.get(0).toPath());
            container.writeTo(bos);
        }
    }

    @Test
    public void testCreateContainerFromExistingAndAlteredTempFile_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Created Container did not pass internal verification");
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(new byte[3]), "qwerty", "qwert.file");
                ContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "qwerty file", "qwerty.domain.com");
                Container container = getContainer(CONTAINER_WITH_NON_REMOVABLE_ANNOTATION)
        ) {
            List<File> tempFiles = getKsieTempFiles();
            //One KSIE directory for container, one per signatureContent and one KSIE...tmp file for annotation.
            assertTrue("Temp dir contains more than necessary KSIE temporary files, test system is not clean.",tempFiles.size() == container.getSignatureContents().size() + 2);
            for (File tmp : tempFiles) {
                if (tmp.isDirectory()){
                    File[] files = tmp.listFiles();
                    for (File file : files) {
                        try (PrintWriter out = new PrintWriter(file)) {
                            out.write("This is new content for temp file.");
                        }
                    }
                }
            }
            packagingFactory.addSignature(container, Collections.singletonList(document), Collections.singletonList(annotation));
        }
    }

    private boolean anyKsieTempFiles() {
        return !getKsieTempFiles().isEmpty();
    }

    private List<File> getKsieTempFiles() {
        List<File> ksieTempFiles = new LinkedList<>();
        for (File f : tempFiles()) {
            if (isTempFile(f)) {
                ksieTempFiles.add(f);
            }
        }
        return ksieTempFiles;
    }

    private void cleanTempDir() throws IOException {
        for (File f : tempFiles()) {
            if (isTempFile(f)) {
                Util.deleteFileOrDirectory(f.toPath());
            }
        }
    }

    private List<File> tempFiles() {
        File[] list = tmpDir.toFile().listFiles();
        if (list == null) {
            return new LinkedList<>();
        } else {
            return Arrays.asList(list);
        }
    }

    private boolean isTempFile(File s) {
        return s.getName().startsWith(Util.TEMP_DIR_PREFIX) || s.getName().startsWith(Util.TEMP_FILE_PREFIX);
    }
}
