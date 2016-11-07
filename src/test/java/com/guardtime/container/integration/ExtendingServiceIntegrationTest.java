package com.guardtime.container.integration;

import com.guardtime.container.extending.ContainerSignatureExtender;
import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.container.extending.ksi.KsiContainerSignatureExtendingPolicy;
import com.guardtime.container.extending.ksi.PublicationKsiContainerSignatureExtendingPolicy;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.publication.inmemory.PublicationsFilePublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        doExtendingTest(CONTAINER_WITH_MULTIPLE_EXTENDABLE_SIGNATURES, signatureFactory, policy, false);
    }

    @Test
    public void testExtendingWithInvalidSignature() throws Exception {
        ExtendingPolicy policy = Mockito.mock(ExtendingPolicy.class);
        when(policy.getExtendedSignature(Mockito.any(Object.class))).thenReturn(Mockito.mock(KSISignature.class));
        doExtendingTest(CONTAINER_WITH_ONE_DOCUMENT, signatureFactory, policy, false);
    }

    @Test
    public void testExtendingWithNotExtendedSignature_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(CONTAINER_WITH_ONE_DOCUMENT);
        when(mockedSignature.isExtended()).thenReturn(false);
        doExtendingTest(CONTAINER_WITH_ONE_DOCUMENT, signatureFactory, false, mockedSignature);
    }

    @Test
    public void testExtendingWithDifferentInputHash_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(CONTAINER_WITH_ONE_DOCUMENT);
        when(mockedSignature.getInputHash()).thenReturn(new DataHash(HashAlgorithm.SHA2_512, new byte[64]));
        doExtendingTest(CONTAINER_WITH_ONE_DOCUMENT, signatureFactory, false, mockedSignature);
    }

    @Test
    public void testExtendingWithDifferentAggregationTime_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(CONTAINER_WITH_ONE_DOCUMENT);
        when(mockedSignature.getAggregationTime()).thenReturn(new Date());
        doExtendingTest(CONTAINER_WITH_ONE_DOCUMENT, signatureFactory, false, mockedSignature);
    }

    @Test
    public void testExtendingWithDifferentIdentity_Nok() throws Exception {
        KSISignature mockedSignature = getMockedSignature(CONTAINER_WITH_ONE_DOCUMENT);
        when(mockedSignature.getIdentity()).thenReturn("Invalid identity.");
        doExtendingTest(CONTAINER_WITH_ONE_DOCUMENT, signatureFactory, false, mockedSignature);
    }

    private void doExtendingTest(String containerName, SignatureFactory factory, boolean extendedStatusAfterExtending, KSISignature signature) throws Exception {
        ExtendingPolicy mockedPolicy = Mockito.mock(ExtendingPolicy.class);
        when(mockedPolicy.getExtendedSignature(Mockito.any(Object.class))).thenReturn(signature);
        doExtendingTest(containerName, factory, mockedPolicy, extendedStatusAfterExtending);
    }

    private void doExtendingTest(SignatureFactory factory, ExtendingPolicy policy) throws Exception {
        doExtendingTest(CONTAINER_WITH_MULTIPLE_EXTENDABLE_SIGNATURES, factory, policy, true);
    }

    private void doExtendingTest(String containerName, SignatureFactory factory, ExtendingPolicy policy, boolean extendedStatusAfterExtending) throws Exception {
        ContainerSignatureExtender extender = new ContainerSignatureExtender(factory, policy);
        Container container = getContainer(containerName);
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

    private KSISignature getMockedSignature(String containerName) throws Exception {
        Container container = getContainer(containerName);
        KSISignature containerSignature = (KSISignature) container.getSignatureContents().get(0).getContainerSignature().getSignature();

        KSISignature mockedSignature = Mockito.mock(KSISignature.class);
        when(mockedSignature.getAggregationTime()).thenReturn(containerSignature.getAggregationTime());
        when(mockedSignature.getIdentity()).thenReturn(containerSignature.getIdentity());
        when(mockedSignature.isExtended()).thenReturn(true);
        when(mockedSignature.getInputHash()).thenReturn(containerSignature.getInputHash());
        return mockedSignature;
    }
}
