package com.guardtime.container;

import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class BlockChainContainerBuilderTest extends AbstractContainerTest {

    @Mock
    private BlockChainContainer mockedContainer;

    @Mock
    private SignatureFactoryType mockedSignatureFactoryType;

    private ContainerSignature mockedSignature = new ContainerSignature() {
        //TODO remove
        @Override
        public void writeTo(OutputStream output) {
            try {
                output.write("TEST-SIGNATURE".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private ZipContainerPackagingFactory packagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedSignatureFactory.create(Mockito.any(DataHash.class))).thenReturn(mockedSignature);
        ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
        when(mockedSignatureFactory.getSignatureFactoryType()).thenReturn(mockedSignatureFactoryType);
        when(mockedSignatureFactoryType.getSignatureMimeType()).thenReturn("application/ksi-signature");
        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        when(mockedSignatureFactoryType.getName()).thenReturn("Mocked Signature Factory");
        this.packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, manifestFactory);
    }

    @Test
    public void testCreateBuilder() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);
        assertNotNull(builder);
    }

    @Test
    public void testCreateBuilderWithoutPackagingFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new BlockChainContainerBuilder(null);
    }

    @Test
    public void testAddDocumentToContainer() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);
        StreamContainerDocument content = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        builder.withDataFile(content);
        assertEquals(1, builder.getDocuments().size());
    }

    @Test
    public void testAddAnnotationToContainer() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedPackagingFactory);
        builder.withAnnotation(MOCKED_ANNOTATION);
        assertEquals(1, builder.getAnnotations().size());
    }

    @Test
    public void testCreateSignature() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(packagingFactory);
        builder.withDataFile(TEST_DOCUMENT_HELLO_TEXT);
        builder.withDataFile(TEST_DOCUMENT_HELLO_PDF);

        builder.withAnnotation(MOCKED_ANNOTATION);
        BlockChainContainer container = builder.build();
        assertNotNull(container);
        assertNotNull(container.getSignatureContents());
        assertFalse(container.getSignatureContents().isEmpty());
    }

}