package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.parsing.TemporaryFileBasedParsingStoreFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.guardtime.ksi.util.Util.toByteArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public abstract class AbstractContentHandlerTest {

    @Mock
    protected ContainerManifestFactory mockManifestFactory;
    @Mock
    private ManifestFactoryType mockManifestFactoryType;

    protected ContentHandler handler;
    protected ParsingStore store;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockManifestFactoryType.getManifestFileExtension()).thenReturn("tlv");
        when(mockManifestFactory.getManifestFactoryType()).thenReturn(mockManifestFactoryType);
        this.store = new TemporaryFileBasedParsingStoreFactory().build();
    }

    @Test
    public void testGetUnrequestedFiles() throws Exception {
        String requestedFileName = "name.txt";
        String requestedStreamContent = "SomeStreamOne";

        String unrequestedFileName = "awesomesouce2.txt";
        String unrequestedStreamContent = "TooAwesomeToBeUsed";

        InputStream mockStream = new ByteArrayInputStream(requestedStreamContent.getBytes());
        handler.add(requestedFileName, mockStream);
        mockStream.close();
        mockStream = new ByteArrayInputStream(unrequestedStreamContent.getBytes());
        handler.add(unrequestedFileName, mockStream);
        handler.get(requestedFileName);

        List unrequested = handler.getUnrequestedFiles();
        assertEquals(1, unrequested.size());
        UnknownDocument doc = (UnknownDocument) unrequested.get(0);
        assertFalse(doc.getFileName().equals(requestedFileName));
        assertTrue(doc.getFileName().equals(unrequestedFileName));
        try(InputStream inputStream = doc.getInputStream()) {
            Arrays.equals(unrequestedStreamContent.getBytes(), toByteArray(inputStream));
        }
    }

    @Test
    public void testGetNames() throws Exception {
        String name1 = "name.txt";
        String name2 = "awesomesouce2.txt";

        handler.add(name1, Mockito.mock(InputStream.class));
        handler.add(name2, Mockito.mock(InputStream.class));

        Set names = handler.getNames();

        assertTrue(names.contains(name1));
        assertTrue(names.contains(name2));
    }

}
