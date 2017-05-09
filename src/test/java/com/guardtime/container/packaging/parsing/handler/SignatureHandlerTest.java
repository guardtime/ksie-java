package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SignatureHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_SIGNATURE_PATH = "/META-INF/signature-1.ksig";
    private static final String INVALID_SIGNATURE_PATH = "funky_music.mp3";

    @Mock
    private SignatureFactory mockSignatureFactory;

    @Mock
    private SignatureFactoryType mockFactoryType;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockFactoryType.getSignatureFileExtension()).thenReturn("ksig");
        when(mockSignatureFactory.getSignatureFactoryType()).thenReturn(mockFactoryType);
        handler = new SignatureHandler(mockSignatureFactory, store);
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