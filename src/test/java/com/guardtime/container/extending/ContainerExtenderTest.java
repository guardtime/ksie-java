package com.guardtime.container.extending;

import com.guardtime.container.AbstractContainerTest;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

public class ContainerExtenderTest extends AbstractContainerTest {

    @Test
    public void testCreateContainerExtender_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature extender must be present");
        ContainerExtender extender = new ContainerExtender(null);
    }

    @Test
    public void testCreateContainerExtenderOK() throws Exception {
        ContainerExtender extender = new ContainerExtender(Mockito.mock(SignatureExtender.class));
        assertNotNull(extender);
    }
}