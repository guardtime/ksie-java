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

package com.guardtime.container.extending.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Test;
import org.mockito.Mockito;

public class KsiContainerSignatureExtendingPolicyTest extends AbstractContainerTest {

    @Test
    public void testCreatingKsiContainerSignatureExtenderWithoutKsi_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new KsiContainerSignatureExtendingPolicy(null);
    }

    @Test
    public void testExtendingDelegatesToKsi() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        ExtendingPolicy<KSISignature> extendingPolicy = new KsiContainerSignatureExtendingPolicy(mockKsi);
        extendingPolicy.getExtendedSignature(Mockito.mock(KSISignature.class));
        Mockito.verify(mockKsi, Mockito.times(1)).extend(Mockito.any(KSISignature.class));
    }

}