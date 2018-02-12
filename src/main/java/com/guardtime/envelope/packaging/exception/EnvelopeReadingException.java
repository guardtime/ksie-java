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

package com.guardtime.envelope.packaging.exception;

import com.guardtime.envelope.packaging.Envelope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnvelopeReadingException extends InvalidPackageException {
    private final List<Throwable> exceptions = new ArrayList<>();
    private Envelope envelope;

    public EnvelopeReadingException(String message) {
        super(message);
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public void addException(Throwable t) {
        exceptions.add(t);
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public void addExceptions(Collection<Throwable> throwables) {
        exceptions.addAll(throwables);
    }
}
