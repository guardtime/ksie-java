package com.guardtime.container.integration;

import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ContainerCloseableIntegrationTest {
    private Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
    private ContainerPackagingFactory packagingFactory;

    @Before
    public void setUp() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        when(mockKsi.sign(Mockito.any(DataHash.class))).thenReturn(Mockito.mock(KSISignature.class));
        when(mockKsi.extend(Mockito.any(KSISignature.class))).thenReturn(Mockito.mock(KSISignature.class));
        SignatureFactory signatureFactory = new KsiSignatureFactory(mockKsi);
        packagingFactory = new ZipContainerPackagingFactory(signatureFactory, new TlvContainerManifestFactory());
    }

    @Test
    public void testClosingClosedContainerDoesNotThrowException() throws Exception {
        File file = loadFile();
        FileInputStream input = new FileInputStream(file);
        Container container = packagingFactory.read(input);
        input.close();
        container.close();
        container.close();
    }

    @Test
    public void testCloseDeletesTemporaryFiles() throws Exception {
        assertFalse("Unclean test system! There are some 'ksie' files in " + tmpDir, anyKsieTempFiles());
        File file = loadFile();
        FileInputStream input = new FileInputStream(file);
        Container container = packagingFactory.read(input);
        input.close();
        assertTrue("Temporary files not found!", anyKsieTempFiles());
        container.close();
        assertFalse("Close did not delete all temporary files!", anyKsieTempFiles());
    }

    @Test
    public void testContainerWithTryWithResources() throws Exception {
        assertFalse("Unclean test system! There are some 'ksie' files in " + tmpDir, anyKsieTempFiles());
        File file = loadFile();
        FileInputStream input = new FileInputStream(file);
        try(Container container = packagingFactory.read(input)) {
            assertFalse(container.getSignatureContents().isEmpty());
            assertTrue("Temporary files not found!", anyKsieTempFiles());
        }
        input.close();
        assertFalse("Close did not delete all temporary files!", anyKsieTempFiles());
    }

    private boolean anyKsieTempFiles() {
        String[] list = tmpDir.toFile().list();
        if (list == null) {
            return false;
        }
        for (String s : Arrays.asList(list)) {
            if (s.startsWith(Util.TEMP_DIR_PREFIX) || s.startsWith(Util.TEMP_FILE_PREFIX))
                return true;
        }
        return false;
    }

    protected File loadFile() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("containers/container-one-document.ksie");
        return new File(url.toURI());
    }
}
