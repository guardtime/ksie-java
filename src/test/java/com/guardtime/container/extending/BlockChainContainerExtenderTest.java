package com.guardtime.container.extending;

import com.guardtime.container.AbstractContainerTest;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

public class BlockChainContainerExtenderTest extends AbstractContainerTest {

    @Test
    public void testCreateBlockChainContainerExtender_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature extender must be present");
        BlockChainContainerExtender extender = new BlockChainContainerExtender(null);
    }

    @Test
    public void testCreateBlockChainContainerExtenderOK() throws Exception {
        BlockChainContainerExtender extender = new BlockChainContainerExtender(Mockito.mock(SignatureExtender.class));
        assertNotNull(extender);
    }
}