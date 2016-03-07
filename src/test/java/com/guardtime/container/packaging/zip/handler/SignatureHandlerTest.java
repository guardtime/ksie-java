package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.signature.SignatureFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class SignatureHandlerTest {

    private static final String VALID_SIGNATURE_PATH = "/META-INF/signature1.ksig";
    private static final String INVALID_SIGNATURE_PATH = "funky_music.mp3";
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
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_SIGNATURE_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_SIGNATURE_PATH));
    }
}