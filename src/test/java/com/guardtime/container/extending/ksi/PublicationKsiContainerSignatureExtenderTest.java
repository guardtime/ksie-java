package com.guardtime.container.extending.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.publication.PublicationRecord;
import org.junit.Test;
import org.mockito.Mockito;

public class PublicationKsiContainerSignatureExtenderTest extends AbstractContainerTest {

    @Test
    public void testPublicationKsiContainerSignatureExtenderWithoutKsi_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new PublicationKsiContainerSignatureExtender(null, Mockito.mock(PublicationRecord.class));
    }

    @Test
    public void testPublicationKsiContainerSignatureExtenderWithoutPublicationRecord_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Publication record");
        new PublicationKsiContainerSignatureExtender(Mockito.mock(KSI.class), null);
    }

}