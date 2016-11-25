package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.packaging.zip.parsing.ParsingStore;
import com.guardtime.container.util.Pair;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public abstract class AbstractContentHandlerTest {

    @Mock
    protected ContainerManifestFactory mockManifestFactory;

    @Mock
    private ManifestFactoryType mockManifestFactoryType;

    @Mock
    protected ParsingStore mockStore;

    protected ContentHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockManifestFactoryType.getManifestFileExtension()).thenReturn("tlv");
        when(mockManifestFactory.getManifestFactoryType()).thenReturn(mockManifestFactoryType);
    }

    @Ignore
    @Test
    public void testGetUnrequestedFiles() throws Exception {
        Pair<String, InputStream> requestablePathFilePair = Pair.of("name.txt", Mockito.mock(InputStream.class));
        Pair<String, InputStream> unrequestedPathFilePair = Pair.of("awesomesouce2.txt", Mockito.mock(InputStream.class));

        handler.add(requestablePathFilePair.getLeft(), requestablePathFilePair.getRight());
        handler.add(unrequestedPathFilePair.getLeft(), unrequestedPathFilePair.getRight());
        handler.get(requestablePathFilePair.getLeft());

        List<Pair<String, File>> unrequested = handler.getUnrequestedFiles();
        assertFalse(unrequested.contains(requestablePathFilePair));
        assertTrue(unrequested.contains(unrequestedPathFilePair));
    }

    @Ignore
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
