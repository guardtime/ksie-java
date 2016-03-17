//package com.guardtime.container.integration;
//
//
//import com.guardtime.container.BlockChainContainerBuilder;
//import com.guardtime.container.manifest.ContainerManifestFactory;
//import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
//import com.guardtime.container.packaging.BlockChainContainer;
//import com.guardtime.container.packaging.SignatureContent;
//import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
//import com.guardtime.container.signature.SignatureFactory;
//import com.guardtime.container.signature.ksi.KsiSignatureFactory;
//import com.guardtime.ksi.KSI;
//import com.guardtime.ksi.KSIBuilder;
//import com.guardtime.ksi.service.client.KSIServiceCredentials;
//import com.guardtime.ksi.service.client.http.HttpClientSettings;
//import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
//import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.ByteArrayInputStream;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//public class ZipContainerIT {
//
//    private ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
//    private ZipContainerPackagingFactory packagingFactory;
//    private SignatureFactory signatureFactory;
//
//    @Before
//    public void setUp() throws Exception {
//        HttpClientSettings settings = new HttpClientSettings("http://ksigw.test.guardtime.com:3333/gt-signingservice",
//                "http://ksigw.test.guardtime.com:8010/gt-extendingservice", "http://verify.guardtime.com/ksi-publications.bin",
//                new KSIServiceCredentials("anon", "anon"));
//        SimpleHttpClient httpClient = new SimpleHttpClient(settings);
//        KSI ksi = new KSIBuilder()
//                .setKsiProtocolSignerClient(httpClient)
//                .setKsiProtocolExtenderClient(httpClient)
//                .setKsiProtocolPublicationsFileClient(httpClient)
//                .setPublicationsFileTrustedCertSelector(new X509CertificateSubjectRdnSelector("E=publications@guardtime.com"))
//                .build();
//        signatureFactory = new KsiSignatureFactory(ksi);
//        packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
//    }
//
//    @Test
//    public void testCreateContainer() throws Exception {
//        BlockChainContainer container = new BlockChainContainerBuilder(packagingFactory)
//                .withDataFile(new ByteArrayInputStream("Test_Data".getBytes()), "test.txt", "application/txt")
//                .build();
//
//        List<SignatureContent> contents = container.getSignatureContents();
//        assertNotNull(contents);
//        assertEquals(1, contents.size());
//    }
//
//    @Test
//    public void testReadContainer() throws Exception {
//        BlockChainContainer container = packagingFactory.read(Files.newInputStream(Paths.get(ClassLoader.getSystemResource("containers/container-one-file.ksie").toURI())));
//        List<SignatureContent> contents = container.getSignatureContents();
//        assertNotNull(contents);
//        assertEquals(1, contents.size());
//    }
//
//}
