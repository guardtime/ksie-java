package com.guardtime.container;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;
import org.junit.Before;

public abstract class AbstractCommonIntegrationTest {
    protected static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-file.ksie";
    protected static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    protected static final String CONTAINER_WITH_EXTRA_FILES = "containers/container-extra-files.ksie";
    protected static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    protected static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";

    private static final String TEST_SIGNING_SERVICE = "http://ksigw.test.guardtime.com:3333/gt-signingservice";
    private static final String TEST_EXTENDING_SERVICE = "http://ksigw.test.guardtime.com:8010/gt-extendingservice";
    private static final String GUARDTIME_PUBLICATIONS_FILE = "http://verify.guardtime.com/ksi-publications.bin";
    private static final KSIServiceCredentials KSI_SERVICE_CREDENTIALS = new KSIServiceCredentials("anon", "anon");
    private ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();

    protected SignatureFactory signatureFactory;
    protected ZipContainerPackagingFactory packagingFactory;
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
