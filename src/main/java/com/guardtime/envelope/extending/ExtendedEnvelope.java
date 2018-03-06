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
import java.util.Collections;
import java.util.List;

/**
 * Wrapper for a {@link Envelope} that has been processed by {@link EnvelopeSignatureExtender}.
 * Provides helper methods to view extending status.
 */
public class ExtendedEnvelope extends Envelope {

    private List<ExtendedSignatureContent> extendedSignatureContents;

    public ExtendedEnvelope(Envelope original) {
        super(original);
        wrapSignatureContents();
    }

    /**
     * @return True, if all {@link SignatureContent}s of this {@link Envelope} are extended.
     */
    public boolean isFullyExtended() {
        for (ExtendedSignatureContent content : extendedSignatureContents) {
            if (!content.isExtended()) {
                return false;
            }
        }
        return true;
    }

    public List<ExtendedSignatureContent> getExtendedSignatureContents() {
        return Collections.unmodifiableList(extendedSignatureContents);
    }

    @Override
    public void add(SignatureContent content) throws EnvelopeMergingException {
        super.add(content);
        wrapSignatureContents();
    }

    @Override
    public void add(Envelope envelope) throws EnvelopeMergingException {
        super.add(envelope);
        wrapSignatureContents();
    }

    @Override
    public void addAll(Collection<SignatureContent> contents) throws EnvelopeMergingException {
        super.addAll(contents);
        wrapSignatureContents();
    }

    private void wrapSignatureContents() {
        List<SignatureContent> originalSignatureContents = getSignatureContents();
        List<ExtendedSignatureContent> extendedContents = new ArrayList<>(originalSignatureContents.size());
        for (SignatureContent content : originalSignatureContents) {
            extendedContents.add(new ExtendedSignatureContent(content));
        }
        this.extendedSignatureContents = extendedContents;
    }
}
