package com.guardtime.container.extending;

import com.guardtime.container.AbstractCommonIntegrationTest;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ExtendingIT extends AbstractCommonIntegrationTest{

    @Test
    public void testExtending() throws Exception {
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        ContainerExtender extender = new ContainerExtender(new KsiSignatureExtender(mockKsi));
        Container extendedContainer = extender.extend(container);

        assertNotNull(extendedContainer);
        verify(mockKsi, atLeast(2)).extend(Mockito.any(KSISignature.class));
        for (SignatureContent content : extendedContainer.getSignatureContents()) {
            assertNotNull(content.getSignature());
            assertNotNull(((KsiContainerSignature) content.getSignature()).getSignature());
        }
    }

    private Container getContainer(String path) throws Exception {
        InputStream input = new FileInputStream(loadFile(path));
        return packagingFactory.read(input);
    }
}
