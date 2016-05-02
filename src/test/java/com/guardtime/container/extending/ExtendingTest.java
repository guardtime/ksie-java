package com.guardtime.container.extending;

import com.guardtime.container.extending.ksi.KsiSignatureExtender;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ExtendingTest {
    private static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    private ZipContainerPackagingFactory factory;

    @Mock
    private KSI mockKSI;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockKSI.read(Mockito.any(byte[].class))).thenReturn(Mockito.mock(KSISignature.class));
        when(mockKSI.extend(Mockito.any(KSISignature.class))).thenReturn(Mockito.mock(KSISignature.class));

        TlvContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
        KsiSignatureFactory signatureFactory = new KsiSignatureFactory(mockKSI);
        factory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
    }

    @Test
    public void testExtending() throws Exception {
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        ContainerExtender extender = new ContainerExtender(new KsiSignatureExtender(mockKSI));
        Container extendedContainer = extender.extend(container);

        assertNotNull(extendedContainer);
        verify(mockKSI, atLeast(2)).extend(Mockito.any(KSISignature.class));
        for (SignatureContent content : extendedContainer.getSignatureContents()) {
            assertNotNull(content.getSignature());
            assertNotNull(((KsiContainerSignature) content.getSignature()).getSignature());
        }
    }

    private Container getContainer(String path) throws IOException, URISyntaxException, InvalidPackageException {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(path).toURI()));
        return factory.read(input);
    }
}
