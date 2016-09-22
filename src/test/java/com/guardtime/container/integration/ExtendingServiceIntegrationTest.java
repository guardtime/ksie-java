package com.guardtime.container.integration;

import com.guardtime.container.extending.ContainerSignatureExtender;
import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.container.extending.ksi.KsiContainerSignatureExtendingPolicy;
import com.guardtime.container.extending.ksi.PublicationKsiContainerSignatureExtendingPolicy;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.publication.inmemory.PublicationsFilePublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ExtendingServiceIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {

    @Test
    public void testExtendingWithKsiContainerSignatureExtender() throws Exception {
        ExtendingPolicy policy = new KsiContainerSignatureExtendingPolicy(ksi);
        doExtendingTest(signatureFactory, policy);
    }

    @Test
    public void testExtendingWithPublicationKsiContainerSignatureExtender() throws Exception {
        PublicationData publicationData = new PublicationData("AAAAAA-CXMCNI-AAJIV3-RB5OEJ-JBK57H-SJ42PI-IB2RE7-2CA2TM-H5W3EF-TF2BX7-HRNRP5-Q2E754"); // June 2016 publication string
        PublicationsFilePublicationRecord publicationRecord = new PublicationsFilePublicationRecord(publicationData);
        ExtendingPolicy policy = new PublicationKsiContainerSignatureExtendingPolicy(ksi, publicationRecord);
        doExtendingTest(signatureFactory, policy);
    }

    @Test
    public void testExtendingWithPublicationKsiContainerSignatureExtender_WithOlderPublicationString() throws Exception {
        PublicationData publicationData = new PublicationData("AAAAAA-CVFWVA-AAPV2S-SN3JLW-YEKPW3-AUSQP6-PF65K5-KVGZZA-7UYTOV-27VX54-VVJQFG-VCK6GR"); // Apr 2015 publication string
        PublicationsFilePublicationRecord publicationRecord = new PublicationsFilePublicationRecord(publicationData);
        ExtendingPolicy policy = new PublicationKsiContainerSignatureExtendingPolicy(ksi, publicationRecord);
        doExtendingTest(signatureFactory, policy, false);
    }

    @Test
    public void testExtendingWithInvalidSignature() throws Exception {
        ExtendingPolicy policy = Mockito.mock(ExtendingPolicy.class);
        when(policy.getExtendedSignature(Mockito.any(Object.class))).thenReturn(Mockito.mock(KSISignature.class));
        doExtendingTest(signatureFactory, policy, false);
    }

    private void doExtendingTest(SignatureFactory factory, ExtendingPolicy policy) throws Exception {
        doExtendingTest(factory, policy, true);
    }

    private void doExtendingTest(SignatureFactory factory, ExtendingPolicy policy, boolean extendedStatusAfterExtending) throws Exception {
        ContainerSignatureExtender extender = new ContainerSignatureExtender(factory, policy);
        Container container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        assertSignaturesExtendedStatus(container, false);
        extender.extend(container);
        assertSignaturesExtendedStatus(container, extendedStatusAfterExtending);
    }

    private Container getContainer(String path) throws Exception {
        InputStream input = new FileInputStream(loadFile(path));
        return packagingFactory.read(input);
    }

    private void assertSignaturesExtendedStatus(Container container, boolean status) {
        for (SignatureContent content : container.getSignatureContents()) {
            assertNotNull(content.getContainerSignature());
            KSISignature signature = (KSISignature) content.getContainerSignature().getSignature();
            assertNotNull(signature);
            assertEquals(status, signature.isExtended());
        }
    }
}
