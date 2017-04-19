package com.guardtime.container.extending;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SignatureReference;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Pair;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ContainerSignatureExtenderTest extends AbstractContainerTest {
    private SignatureFactory mockSignatureFactory;
    private ContainerSignatureExtender extender;
    private ContainerSignature mockSignature;

    @Before
    public void setUp() throws Exception {
        mockSignatureFactory = mock(SignatureFactory.class);
        mockSignature = mock(ContainerSignature.class);
        extender = new ContainerSignatureExtender(mockSignatureFactory, mock(ExtendingPolicy.class));
    }

    private Container makeMockContainer() throws Exception {
        Container mockContainer = mock(Container.class);
        SignatureContent mockSignatureContent = mock(SignatureContent.class);
        Manifest mockManifest = mock(Manifest.class);
        doReturn(mockSignature).when(mockSignatureContent).getContainerSignature();
        doReturn(Arrays.asList(mockSignatureContent)).when(mockContainer).getSignatureContents();
        doReturn(mock(SignatureReference.class)).when(mockManifest).getSignatureReference();
        doReturn(Pair.of("str", mockManifest)).when(mockSignatureContent).getManifest();
        return mockContainer;
    }

    @Test
    public void testCreateWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory");
        new ContainerSignatureExtender(null, mock(ExtendingPolicy.class));
    }

    @Test
    public void testCreateWithoutExtendingPolicy_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Extending policy");
        new ContainerSignatureExtender(mock(SignatureFactory.class), null);
    }

    @Test
    public void testExtendingSuccess() throws Exception {
        doReturn(true).when(mockSignature).isExtended();
        assertTrue(extender.extend(makeMockContainer()).getSignatureContents().get(0).isExtended());
    }

    @Test
    public void testExtendingFails() throws Exception {
        doThrow(SignatureException.class)
                .when(mockSignatureFactory)
                .extend(Mockito.any(ContainerSignature.class), Mockito.any(ExtendingPolicy.class));
        assertFalse(extender.extend(makeMockContainer()).getSignatureContents().get(0).isExtended());
    }

    @Test
    public void testExtendingIsNotDone() throws Exception {
        doReturn(false).when(mockSignature).isExtended();
        assertFalse(extender.extend(makeMockContainer()).getSignatureContents().get(0).isExtended());
    }

}