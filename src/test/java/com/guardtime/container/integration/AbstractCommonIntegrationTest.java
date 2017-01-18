package com.guardtime.container.integration;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.ContainerReadingException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.packaging.zip.handler.ContentParsingException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;
import com.guardtime.ksi.util.Util;

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

public abstract class AbstractCommonIntegrationTest extends AbstractContainerTest {

    protected ContainerPackagingFactory packagingFactory;
    protected SignatureFactory signatureFactory;
    protected KSI ksi;

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
        packagingFactory = new ZipContainerPackagingFactoryBuilder().withSignatureFactory(signatureFactory).build();

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

    Container getContainerIgnoreExceptions(String container) throws Exception {
        try {
            return getContainer(container);
        } catch (ContainerReadingException e) {
            return e.getContainer();
        }
    }

    byte[] addDocumentsToExistingContainer_SkipDuplicate(byte[] zipFile, List<Pair<byte[], String>> files) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (
                ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipFile));
                ZipOutputStream out = new ZipOutputStream(bos)
        ) {
            ZipEntry entry;
            List<String> filesInZip = new LinkedList<>();
            while ((entry = zin.getNextEntry()) != null) {
                filesInZip.add(entry.getName());
                writeFromInputToZipOutput(out, zin, entry.getName());
            }
            for (Pair pair : files) {
                if (!filesInZip.contains(pair.getRight())) {
                    try (InputStream in = new ByteArrayInputStream((byte[]) pair.getLeft())) {
                        writeFromInputToZipOutput(out, in, (String) pair.getRight());
                    }
                }
            }
        }
        return bos.toByteArray();
    }

    /*
    Closes ZipOutputStream entry.
     */
    private void writeFromInputToZipOutput(ZipOutputStream out, InputStream in, String fileName) throws IOException {
        out.putNextEntry(new ZipEntry(fileName));
        Util.copyData(in, out);
        out.closeEntry();
    }
}
