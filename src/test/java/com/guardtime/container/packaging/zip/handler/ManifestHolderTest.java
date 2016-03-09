package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.Assert.*;

public class ManifestHolderTest {

    @Mock
    private ContainerManifestFactory mockManifestFactory;

    @Mock
    private File mockFile;

    private ManifestHolder handler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new ManifestHolder(mockManifestFactory);
    }

    @Test
    public void testIndexExtraction() throws Exception {
        assertEquals(0, handler.getMaxIndex());
        int index = 7;
        handler.add("/META-INF/manifest" + index + ".tlv", mockFile);
        assertEquals(index, handler.getMaxIndex());
    }
}