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

package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MimeTypeIntegrityRuleTest extends AbstractContainerTest {

    @Mock
    private ContainerPackagingFactory mockedPackagingFactory;

    private Rule rule;
    private byte[] mimetypeContent = "someValueAsMimetype".getBytes(StandardCharsets.UTF_8);

    @Before
    public void setUpRule() {
        when(mockedPackagingFactory.getMimeTypeContent()).thenReturn(mimetypeContent);
        this.rule = new MimeTypeIntegrityRule(defaultRuleStateProvider, mockedPackagingFactory);
    }

    @Test
    public void testVerifyInvalidMimetypeContent_ThrowsRuleTerminatingException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("MIME type integrity could not be verified for");
        MimeType mockMimetype = getMimeType("NotTheMimeTypeValue".getBytes(StandardCharsets.UTF_8));
        Container mockContainer = Mockito.mock(Container.class);
        when(mockContainer.getMimeType()).thenReturn(mockMimetype);
        rule.verify(new ResultHolder(), mockContainer);
    }

    @Test
    public void testVerifyValidMimetypeResultsInOK() throws Exception {
        when(mockedPackagingFactory.getMimeTypeContent()).thenReturn(mimetypeContent);
        MimeType mockMimetype = getMimeType(mimetypeContent);
        Container mockContainer = Mockito.mock(Container.class);
        when(mockContainer.getMimeType()).thenReturn(mockMimetype);
        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockContainer);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private MimeType getMimeType(final byte[] mimetypeContent) {
        return new MimeType() {
            @Override
            public String getUri() {
                return "SomeUri";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(mimetypeContent);
            }
        };
    }

}