package com.guardtime.container.indexing;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class UuidIndexProviderFactory implements IndexProviderFactory {

    @Override
    public IndexProvider create() throws IndexingException {
        return new UuidIndexProvider();
    }

    @Override
    public IndexProvider create(Container container) throws IndexingException {
        return new UuidIndexProvider(container);
    }

    class UuidIndexProvider implements IndexProvider {

        UuidIndexProvider() {
        }

        UuidIndexProvider(Container container) throws IndexingException {
            for (SignatureContent signatureContent : container.getSignatureContents()) {
                Set<String> uris = getUriSet(signatureContent);
                verifyUuidExistence(uris);
            }
        }

        @Override
        public String getNextDocumentsManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextAnnotationsManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextSignatureIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextSingleAnnotationManifestIndex() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getNextAnnotationIndex() {
            return UUID.randomUUID().toString();
        }

        private Set<String> getUriSet(SignatureContent signatureContent) {
            Set<String> uris = new HashSet<>();

            Manifest manifest = signatureContent.getManifest().getRight();
            if (manifest != null && manifest.getSignatureReference() != null) {
                uris.add(manifest.getSignatureReference().getUri());
            }
            uris.add(signatureContent.getManifest().getLeft());
            uris.add(signatureContent.getDocumentsManifest().getLeft());
            uris.add(signatureContent.getAnnotationsManifest().getLeft());
            uris.addAll(signatureContent.getSingleAnnotationManifests().keySet());
            uris.addAll(signatureContent.getAnnotations().keySet());
            return uris;
        }

        private void verifyUuidExistence(Set<String> set) throws IndexingException {
            for (String str : set) {
                str = str.substring(str.lastIndexOf("/") + 1);
                String index = str.substring(str.indexOf("-") + 1, str.lastIndexOf("."));
                try {
                    UUID.fromString(index);
                } catch (IllegalArgumentException e) {
                    throw new IndexingException("Not a RFC4122 UUID based index");
                }
            }
        }
    }
}
