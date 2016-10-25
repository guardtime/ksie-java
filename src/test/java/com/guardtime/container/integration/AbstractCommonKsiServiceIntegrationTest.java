package com.guardtime.container.integration;

import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;

import org.junit.Before;

public abstract class AbstractCommonKsiServiceIntegrationTest extends AbstractCommonIntegrationTest {

    private static final String TEST_SIGNING_SERVICE = "http://ksigw.test.guardtime.com:8080/gt-signingservice";
    private static final String TEST_EXTENDING_SERVICE = "http://ksigw.test.guardtime.com:8081/gt-extendingservice";
    private static final String GUARDTIME_PUBLICATIONS_FILE = "http://verify.guardtime.com/ksi-publications.bin";
    private static final KSIServiceCredentials KSI_SERVICE_CREDENTIALS = new KSIServiceCredentials("anon", "anon");
    protected KSI ksi;

    @Before
    public void setUp() throws Exception {
        HttpClientSettings settings = new HttpClientSettings(
                TEST_SIGNING_SERVICE,
                TEST_EXTENDING_SERVICE,
                GUARDTIME_PUBLICATIONS_FILE,
                KSI_SERVICE_CREDENTIALS
        );
        SimpleHttpClient httpClient = new SimpleHttpClient(settings);
        ksi = new KSIBuilder()
                .setKsiProtocolSignerClient(httpClient)
                .setKsiProtocolExtenderClient(httpClient)
                .setKsiProtocolPublicationsFileClient(httpClient)
                .setPublicationsFileTrustedCertSelector(new X509CertificateSubjectRdnSelector("E=publications@guardtime.com"))
                .build();
        signatureFactory = new KsiSignatureFactory(ksi);
        packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
    }
}
