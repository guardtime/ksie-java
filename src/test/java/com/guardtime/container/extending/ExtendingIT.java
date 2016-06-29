package com.guardtime.container.extending;

import com.guardtime.container.AbstractCommonIntegrationTest;
import com.guardtime.container.extending.ksi.KsiSignatureExtender;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExtendingIT extends AbstractCommonIntegrationTest {

    @Test
    public void testExtending() throws Exception {
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        ContainerExtender extender = new ContainerExtender(new KsiSignatureExtender(mockKsi));
        Container extendedContainer = extender.extend(container);

        assertNotNull(extendedContainer);
        verify(mockKsi, atLeast(2)).extend(Mockito.any(KSISignature.class));
        for (SignatureContent content : extendedContainer.getSignatureContents()) {
            assertNotNull(content.getContainerSignature());
            assertNotNull(content.getContainerSignature().getSignature());
        }
    }

    private Container getContainer(String path) throws Exception {
        when(mockKsi.read(Mockito.any(InputStream.class))).thenReturn(Mockito.mock(KSISignature.class));
        InputStream input = new FileInputStream(loadFile(path));
        return packagingFactory.read(input);
    }
}
