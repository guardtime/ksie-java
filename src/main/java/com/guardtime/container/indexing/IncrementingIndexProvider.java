package com.guardtime.container.indexing;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import org.mockito.internal.util.collections.Sets;

import java.util.HashSet;
import java.util.Set;

public class IncrementingIndexProvider implements IndexProvider {
    private int documentsManifestIndex = 0;
    private int manifestIndex = 0;
    private int signatureIndex = 0;
    private int annotationsManifestIndex = 0;
    private int singleAnnotationManifestIndex = 0;
    private int annotationIndex = 0;

    @Override
    public String getNextDocumentsManifestIndex() {
        return Integer.toString(++documentsManifestIndex);
    }

    @Override
    public String getNextManifestIndex() {
        return Integer.toString(++manifestIndex);
    }

    @Override
    public String getNextAnnotationsManifestIndex() {
        return Integer.toString(++annotationsManifestIndex);
    }

    @Override
    public String getNextSignatureIndex() {
        return Integer.toString(++signatureIndex);
    }

    @Override
    public String getNextSingleAnnotationManifestIndex() {
        return Integer.toString(++singleAnnotationManifestIndex);
    }

    @Override
    public String getNextAnnotationIndex() {
        return Integer.toString(++annotationIndex);
    }

    @Override
    public void updateIndexes(Container container) throws IndexingException {
        int maxIndex = 0;
        int maxAnnotationIndex = 0;
        for (SignatureContent content : container.getSignatureContents()) {
            Set<String> manifestUriSet = Sets.newSet(
                    content.getManifest().getLeft(),
                    content.getDocumentsManifest().getLeft(),
                    content.getAnnotationsManifest().getLeft()
            );
            Manifest manifest = content.getManifest().getRight();
            if(manifest != null && manifest.getSignatureReference() != null) {
                manifestUriSet.add(manifest.getSignatureReference().getUri());
            }
            Set<String> annotationUriSet = new HashSet<>(content.getSingleAnnotationManifests().keySet());
            annotationUriSet.addAll(content.getAnnotations().keySet());
            maxIndex = compareAndUpdate(manifestUriSet, maxIndex);
            maxAnnotationIndex = compareAndUpdate(annotationUriSet, maxAnnotationIndex);
        }
        manifestIndex = maxIndex;
        signatureIndex = maxIndex;
        documentsManifestIndex = maxIndex;
        annotationsManifestIndex = maxIndex;
        singleAnnotationManifestIndex = maxAnnotationIndex;
        annotationIndex = maxAnnotationIndex;
    }

    private int compareAndUpdate(String str, int value) throws IndexingException {
        int tmp = getIndex(str);
        if (value < tmp) {
            return tmp;
        }
        return value;
    }

    private int compareAndUpdate(Set<String> set, int value) throws IndexingException {
        for (String str : set) {
            value = compareAndUpdate(str, value);
        }
        return value;
    }

    private int getIndex(String str) throws IndexingException {
        str = str.substring(str.lastIndexOf("/") + 1);
        String index = str.substring(str.indexOf("-") + 1, str.lastIndexOf("."));
        if (!index.equals(index.replaceAll("[^0-9]", ""))) {
            throw new IndexingException("Not an integer based index");
        }
        return new Integer(index);
    }
}
