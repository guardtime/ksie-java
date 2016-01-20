package com.guardtime.container;

import com.guardtime.container.datafile.StreamContainerDocument;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BlockChainContainerBuilderTest extends AbstractBlockChainContainerTest {

    @Test
    public void testCreateBuilder() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedSignatureFactory, mockedManifestFactory, mockedPackagingFactory);
        assertNotNull(builder);
    }

    @Test
    public void testCreateBuilderWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory must be present");
        new BlockChainContainerBuilder(null, mockedManifestFactory, mockedPackagingFactory);
    }

    @Test
    public void testCreateBuilderWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new BlockChainContainerBuilder(mockedSignatureFactory, null, mockedPackagingFactory);
    }

    @Test
    public void testCreateBuilderWithoutPackagingFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new BlockChainContainerBuilder(mockedSignatureFactory, mockedManifestFactory, null);
    }

    @Test
    public void testAddDocumentFromStream() throws Exception {
        BlockChainContainerBuilder builder = new BlockChainContainerBuilder(mockedSignatureFactory, mockedManifestFactory, mockedPackagingFactory);
        StreamContainerDocument content = new StreamContainerDocument(new ByteArrayInputStream("TEST_FILE1".getBytes()), "test1.txt", "application/txt");
        builder.withDataFile(content);
        assertEquals(1, builder.getDocuments().size());
    }

}