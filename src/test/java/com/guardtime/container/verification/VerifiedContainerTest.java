package com.guardtime.container.verification;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.ResultHolder;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifiedContainerTest {

    @Test
    public void testWrappedMethodsProvideAccessToOriginals() {
        Container originalContainer = setUpMockedContainer();
        VerifiedContainer verifiedContainer = new VerifiedContainer(originalContainer, new ResultHolder());
        assertEquals(originalContainer.getMimeType(), verifiedContainer.getMimeType());
        assertEquals(originalContainer.getUnknownFiles(), verifiedContainer.getUnknownFiles());
    }

    @Test
    public void testSignatureContentsAreWrapped() {
        VerifiedContainer verifiedContainer = new VerifiedContainer(setUpMockedContainer(), new ResultHolder());
        for(SignatureContent content : verifiedContainer.getVerifiedSignatureContents()) {
            assertTrue(content instanceof VerifiedSignatureContent);
        }
    }

    private Container setUpMockedContainer() {
        Container mockedContainer = mock(Container.class);
        when(mockedContainer.getMimeType()).thenReturn(mock(MimeType.class));
        when(mockedContainer.getUnknownFiles()).thenReturn(Collections.singletonList(mock(UnknownDocument.class)));
        when(mockedContainer.getSignatureContents()).thenAnswer(new Answer<List<? extends SignatureContent>>() {
            @Override
            public List<? extends SignatureContent> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singletonList(mock(SignatureContent.class));
            }
        });
        return mockedContainer;
    }

}