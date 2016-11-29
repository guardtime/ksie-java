package com.guardtime.container.integration;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class AbstractCommonKsiServiceIntegrationTest extends AbstractContainerTest {

    protected ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
    protected SignatureFactory signatureFactory;
    protected ZipContainerPackagingFactory packagingFactory;

    private static final KSIServiceCredentials KSI_SERVICE_CREDENTIALS;
    private static final String TEST_SIGNING_SERVICE;
    private static final String TEST_EXTENDING_SERVICE;
    private static final String GUARDTIME_PUBLICATIONS_FILE;

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

    Container getContainer() throws Exception {
        return getContainer(CONTAINER_WITH_ONE_DOCUMENT);
    }

    Container getContainer(String container) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(container);
        File file = new File(url.toURI());
        try (FileInputStream input = new FileInputStream(file)) {
            return packagingFactory.read(input);
        }
    }

    byte[] addDocumentsToExistingContainer_SkipDuplicate(byte[] zipFile, List<Pair<byte[], String>> files) throws IOException {
        byte[] buf = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (
                ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipFile));
                ZipOutputStream out = new ZipOutputStream(bos)
        ) {
            ZipEntry entry = zin.getNextEntry();
            List<String> filesInZip = new LinkedList<>();
            while (entry != null) {
                filesInZip.add(entry.getName());
                writeFromInputToZipOutput(buf, out, zin, entry.getName());
                entry = zin.getNextEntry();
            }
            for (Pair pair : files) {
                if (!filesInZip.contains(pair.getRight())) {
                    try (InputStream in = new ByteArrayInputStream((byte[]) pair.getLeft())) {
                        writeFromInputToZipOutput(buf, out, in, (String) pair.getRight());
                        out.closeEntry();
                    }
                }
            }
        }
        return bos.toByteArray();
    }

    private void writeFromInputToZipOutput(byte[] buf, ZipOutputStream out, InputStream in, String fileName) throws IOException {
        out.putNextEntry(new ZipEntry(fileName));
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }
}
