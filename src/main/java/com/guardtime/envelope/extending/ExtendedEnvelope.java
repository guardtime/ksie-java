/*
 * Copyright 2013-2018 Guardtime, Inc.
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

package com.guardtime.envelope.extending;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper for a {@link Envelope} that has been processed by {@link EnvelopeSignatureExtender}.
 * Provides helper methods to view extending status.
 */
public class ExtendedEnvelope extends Envelope {

    public ExtendedEnvelope(Envelope original) {
        super(getWrappedSignatureContents(original.getSignatureContents()), original.getUnknownFiles());
    }

    private static Collection<SignatureContent> getWrappedSignatureContents(Collection<SignatureContent> originalContents) {
        List<SignatureContent> extendedContents = new ArrayList<>(originalContents.size());
        for (SignatureContent content : originalContents) {
            extendedContents.add(new ExtendedSignatureContent(content));
        }
        return extendedContents;
    }

    /**
     * @return True, if all {@link SignatureContent}s of this {@link Envelope} are extended.
     */
    public boolean isFullyExtended() {
        for (ExtendedSignatureContent content : getExtendedSignatureContents()) {
            if (!content.isExtended()) {
                return false;
            }
        }
        return true;
    }

    public List<ExtendedSignatureContent> getExtendedSignatureContents() {
        List<ExtendedSignatureContent> result = new ArrayList<>();
        for (SignatureContent content: getSignatureContents()) {
            if (content instanceof ExtendedSignatureContent) {
                result.add((ExtendedSignatureContent) content);
            } else {
                // wrap?
                result.add(new ExtendedSignatureContent(content));
            }
        }
        return result;
    }

    @Override
    public void add(SignatureContent content) throws EnvelopeMergingException {
        super.add(new ExtendedSignatureContent(content));
    }

    @Override
    public void addAll(Collection<SignatureContent> contents) throws EnvelopeMergingException {
        super.addAll(getWrappedSignatureContents(contents));
    }
}
