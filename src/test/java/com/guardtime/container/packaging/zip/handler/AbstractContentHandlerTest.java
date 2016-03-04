package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class AbstractContentHandlerTest {

    @Mock
    protected ContainerManifestFactory mockManifestFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}
