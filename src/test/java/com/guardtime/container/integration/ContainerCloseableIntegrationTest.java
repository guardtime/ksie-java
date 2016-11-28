package com.guardtime.container.integration;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

public class ContainerCloseableIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {
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
        assertFalse("Close did not delete all temporary files!", anyKsieTempFiles());
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
        for (String tmpFile : getKsieTempFiles()) {
            System.out.println(tmpFile);
        }
        cleanTempDir();
        container.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testDeleteAllTempFilesFromExistingContainer() throws Exception {
        Container existingContainer = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        List<String> ksieTempFiles = getKsieTempFiles();
        try (
            ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(new byte[313]), "byte inputstream", "byte-input-stream.bis");
            ContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "content", "domain.com")
        ) {
            Container container = packagingFactory.create(existingContainer, Collections.singletonList(document), Collections.singletonList(annotation));
            for (String doc : ksieTempFiles) {
                if (isTempFile(doc)) {
                    Util.deleteFileOrDirectory(Paths.get(doc));
                }
            }
            container.close();
        }
        existingContainer.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testDeleteAllTempFilesFromCreatedContainer() throws Exception {
        Container existingContainer = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        List<String> ksieTempFiles = getKsieTempFiles();
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(new byte[313]), "byte inputstream", "byte-input-stream.bis");
                ContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "content", "domain.com")
        ) {
            Container container = packagingFactory.create(existingContainer, Collections.singletonList(document), Collections.singletonList(annotation));
            List<String> ksieTempFiles2 = getKsieTempFiles();
            for (String doc : ksieTempFiles2) {
                if (isTempFile(doc) && !ksieTempFiles.contains(doc)) {
                    Util.deleteFileOrDirectory(Paths.get(doc));
                }
            }
            container.close();
        }
        existingContainer.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testWriteClosedContainer() throws Exception {
        expectedException.expect(FileNotFoundException.class);
        expectedException.expectMessage("The system cannot find the path specified");
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        container.close();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            container.writeTo(bos);
        }
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testWriteContainerWithTempFileRemoved() throws Exception {
        expectedException.expect(FileNotFoundException.class);
        expectedException.expectMessage("The system cannot find the file specified");
        try (
                Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            List<String> tempFiles = getKsieTempFiles();
            assertTrue(tempFiles.size() == 1);
            String[] files = Paths.get(tmpDir + "\\" + tempFiles.get(0)).toFile().list();
            Util.deleteFileOrDirectory(Paths.get(tmpDir + "\\" + tempFiles.get(0) + "\\" + files[0]));
            container.writeTo(bos);
        }
    }

    @Test
    public void testCreateContainerFromExistingAndAlteredTempFile_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Created Container does not pass internal verification");
        try (
                ContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(new byte[3]), "qwerty", "qwert.file");
                ContainerAnnotation annotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "qwerty file", "qwerty.domain.com");
                Container container = getContainer(CONTAINER_WITH_NON_REMOVABLE_ANNOTATION)
        ) {
            List<String> tempFiles = getKsieTempFiles();
            //One KSIE directory for container and one KSIE...tmp file for annotation.
            assertTrue("Temp dir contains more than one KSIE temporary files, test system is not clean.",tempFiles.size() == 2);
            for (String tmp : tempFiles) {
                tmp = tmpDir + "\\" + tmp;
                if (new File(tmp).isDirectory()){
                    String[] files = Paths.get(tmp).toFile().list();
                    for (String file : files) {
                        try (PrintWriter out = new PrintWriter(tmp + "\\" + file)) {
                            out.write("This is new content for temp file.");
                        }
                    }
                }
            }
            packagingFactory.create(container, Collections.singletonList(document), Collections.singletonList(annotation));
        }
    }

    private List<String> getKsieTempFiles() {
        List<String> ksieTempFiles = new LinkedList<>();
        for (String s : tempFiles()) {
            if (isTempFile(s)) {
                ksieTempFiles.add(s);
            }
        }
        return ksieTempFiles;
    }

    private boolean anyKsieTempFiles() {
        return !getKsieTempFiles().isEmpty();
    }

    private void cleanTempDir() throws IOException {
        for (String s : tempFiles()) {
            if (isTempFile(s)) {
                Util.deleteFileOrDirectory(Paths.get(s));
            }
        }
    }

    private List<String> tempFiles() {
        String[] list = tmpDir.toFile().list();
        if (list == null) {
            return new LinkedList<>();
        } else {
            return Arrays.asList(list);
        }
    }

    private boolean isTempFile(String s) {
        return s.startsWith(Util.TEMP_DIR_PREFIX) || s.startsWith(Util.TEMP_FILE_PREFIX);
    }
}
