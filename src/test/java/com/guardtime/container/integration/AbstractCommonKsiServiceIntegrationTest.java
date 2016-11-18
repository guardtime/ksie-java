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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public abstract class AbstractCommonKsiServiceIntegrationTest extends AbstractCommonIntegrationTest {

    private static final String TEST_SIGNING_SERVICE;
    private static final String TEST_EXTENDING_SERVICE;
    private static final String GUARDTIME_PUBLICATIONS_FILE;
    private static final KSIServiceCredentials KSI_SERVICE_CREDENTIALS;

    static {
        try {
            Properties properties = new Properties();
            URL url = Thread.currentThread().getContextClassLoader().getResource("config.properties");
            properties.load(new FileInputStream(new File(url.toURI())));
            TEST_SIGNING_SERVICE = properties.getProperty("service.signing");
            TEST_EXTENDING_SERVICE = properties.getProperty("service.extending");
            GUARDTIME_PUBLICATIONS_FILE = properties.getProperty("publications.file.url");
            KSI_SERVICE_CREDENTIALS = new KSIServiceCredentials(properties.getProperty("credentials.id"), properties.getProperty("credentials.key"));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

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
