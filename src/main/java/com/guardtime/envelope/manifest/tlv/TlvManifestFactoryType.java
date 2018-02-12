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

package com.guardtime.envelope.manifest.tlv;

import com.guardtime.envelope.manifest.ManifestFactoryType;

import java.util.Objects;

class TlvManifestFactoryType implements ManifestFactoryType {

    private final String name;
    private final String manifestFileExtension;

    TlvManifestFactoryType(String name, String manifestFileExtension) {
        this.name = name;
        this.manifestFileExtension = manifestFileExtension;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getManifestFileExtension() {
        return manifestFileExtension;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "name= \'" + name + '\'' +
                ", fileExtension= \'" + manifestFileExtension + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlvManifestFactoryType that = (TlvManifestFactoryType) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(manifestFileExtension, that.manifestFileExtension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, manifestFileExtension);
    }
}
