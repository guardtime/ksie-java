package com.guardtime.container.extending;

import com.guardtime.container.AbstractContainerTest;
import org.junit.Test;

public class BlockChainContainerExtenderTest extends AbstractContainerTest {

    @Test
    public void testCreateBlockChainContainerExtender_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature extender must be present");
        BlockChainContainerExtender extender = new BlockChainContainerExtender(null);
    }
}