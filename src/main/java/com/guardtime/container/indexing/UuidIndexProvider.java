package com.guardtime.container.indexing;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;

import java.util.*;

public class UuidIndexProvider implements IndexProvider {
    private List<UUID> uuidList = new ArrayList<>();
    private List<UUID> annotationUuidList = new ArrayList<>();
    private int documentsManifestIndex = 0;
    private int manifestIndex = 0;
    private int signatureIndex = 0;
    private int annotationsManifestIndex = 0;
    private int singleAnnotationManifestIndex = 0;
    private int annotationIndex = 0;

    @Override
    public String getNextDocumentsManifestIndex() {
        UUID uuid = uuidList.get(documentsManifestIndex);
        if (uuid == null) {
            uuid = uuidList.set(documentsManifestIndex++, UUID.randomUUID());
        }
        return uuid.toString();
    }

    @Override
    public String getNextManifestIndex() {
        UUID uuid = uuidList.get(manifestIndex);
        if (uuid == null) {
            uuid = uuidList.set(manifestIndex++, UUID.randomUUID());
        }
        return uuid.toString();
    }

    @Override
    public String getNextAnnotationsManifestIndex() {
        UUID uuid = uuidList.get(annotationsManifestIndex);
        if (uuid == null) {
            uuid = uuidList.set(annotationsManifestIndex++, UUID.randomUUID());
        }
        return uuid.toString();
    }

    @Override
    public String getNextSignatureIndex() {
        UUID uuid = uuidList.get(signatureIndex);
        if (uuid == null) {
            uuid = uuidList.set(signatureIndex++, UUID.randomUUID());
        }
        return uuid.toString();
    }

    @Override
    public String getNextSingleAnnotationManifestIndex() {
        UUID uuid = annotationUuidList.get(singleAnnotationManifestIndex);
        if (uuid == null) {
            uuid = annotationUuidList.set(singleAnnotationManifestIndex++, UUID.randomUUID());
        }
        return uuid.toString();
    }

    @Override
    public String getNextAnnotationIndex() {
        UUID uuid = annotationUuidList.get(annotationIndex);
        if (uuid == null) {
            uuid = annotationUuidList.set(annotationIndex++, UUID.randomUUID());
        }
        return uuid.toString();
    }

    @Override
    public void updateIndexes(Container container) throws IndexingException {
        // Verify the container is using UUID indexes
        for (SignatureContent signatureContent : container.getSignatureContents()) {
            Set<String> uris = getUriSet(signatureContent);
            verifyUuidExistence(uris);
        }
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
