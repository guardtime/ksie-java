package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.ManifestFactoryType;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public abstract class AbstractContentHandlerTest {

    @Mock
    protected ContainerManifestFactory mockManifestFactory;

    @Mock
    private ManifestFactoryType mockManifestFactoryType;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockManifestFactoryType.getManifestFileExtension()).thenReturn("tlv");
        when(mockManifestFactory.getManifestFactoryType()).thenReturn(mockManifestFactoryType);
    }
}
