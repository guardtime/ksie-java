package com.guardtime.container.extending;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtendedContainerTest {

    @Test
    public void testWrappedMethodsProvideAccessToOriginals() {
        Container originalContainer = setUpMockedContainer();
        ExtendedContainer extendedContainer = new ExtendedContainer(originalContainer);
        assertEquals(originalContainer.getMimeType(), extendedContainer.getMimeType());
        assertEquals(originalContainer.getUnknownFiles(), extendedContainer.getUnknownFiles());
    }

    @Test
    public void testSignatureContentsAreWrapped() {
        ExtendedContainer extendedContainer = new ExtendedContainer(setUpMockedContainer());
        for(SignatureContent content : extendedContainer.getSignatureContents()) {
            assertTrue(content instanceof ExtendedSignatureContent);
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