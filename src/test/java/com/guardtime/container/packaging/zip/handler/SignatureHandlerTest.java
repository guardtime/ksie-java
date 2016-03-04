package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.signature.SignatureFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class SignatureHandlerTest {

    private SignatureHandler handler;

    @Mock
    private SignatureFactory mockSignatureFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new SignatureHandler(mockSignatureFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        String validString = "/META-INF/signature1.ksig";
        assertTrue("Failed to identify supported filename string.", handler.isSupported(validString));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        String validString = "funky_music.mp3";
        assertFalse("Identified unsupported filename string.", handler.isSupported(validString));
    }
}