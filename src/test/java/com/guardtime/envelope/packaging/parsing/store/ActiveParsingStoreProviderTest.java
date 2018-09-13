package com.guardtime.envelope.packaging.parsing.store;

import com.guardtime.envelope.AbstractEnvelopeTest;

import org.junit.Test;

public class ActiveParsingStoreProviderTest extends AbstractEnvelopeTest {

    @Test
    public void testRequestParsingStoreFromUnsetActiveParsingStoreProvider_ThrowsIllegalStateException() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("No ParsingStore has been set");
        ActiveParsingStoreProvider.setActiveParsingStore(null); // Just in case lets set it to null!
        ActiveParsingStoreProvider.getActiveParsingStore();
    }

}
