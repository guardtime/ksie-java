/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

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