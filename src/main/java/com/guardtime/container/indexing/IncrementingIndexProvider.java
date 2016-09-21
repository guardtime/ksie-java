package com.guardtime.container.indexing;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;

import java.util.Set;
import java.util.regex.Pattern;

public class IncrementingIndexProvider implements IndexProvider {
    private int documentsManifestIndex = 0;
    private int manifestIndex = 0;
    private int signatureIndex = 0;
    private int annotationsManifestIndex = 0;
    private int singleAnnotationManifestIndex = 0;
    private int annotationIndex = 0;

    @Override
    public String getNextDocumentsManifestIndex() {
        return incrementAndConvertToString(documentsManifestIndex);
    }

    @Override
    public String getNextManifestIndex() {
        return incrementAndConvertToString(manifestIndex);
    }

    @Override
    public String getNextAnnotationsManifestIndex() {
        return incrementAndConvertToString(annotationsManifestIndex);
    }

    @Override
    public String getNextSignatureIndex() {
        return incrementAndConvertToString(signatureIndex);
    }

    @Override
    public String getNextSingleAnnotationManifestIndex() {
        return incrementAndConvertToString(singleAnnotationManifestIndex);
    }

    @Override
    public String getNextAnnotationIndex() {
        return incrementAndConvertToString(annotationIndex);
    }

    String incrementAndConvertToString(int i) {
        return Integer.toString(++i);
    }

    @Override
    public void updateIndexes(Container container) throws IndexingException {
        int maxIndex = 0;
        int maxAnnotationIndex = 0;
        for (SignatureContent content : container.getSignatureContents()) {
            Pair<String, Manifest> manifest = content.getManifest();
            maxIndex = compareAndUpdate(manifest.getLeft(), maxIndex);
            maxIndex = compareAndUpdate(content.getDocumentsManifest().getLeft(), maxIndex);
            maxIndex = compareAndUpdate(content.getAnnotationsManifest().getLeft(), maxIndex);
            maxIndex = compareAndUpdate(manifest.getRight().getSignatureReference().getUri(), maxIndex);
            maxAnnotationIndex = compareAndUpdate(content.getSingleAnnotationManifests().keySet(), maxAnnotationIndex);
            maxAnnotationIndex = compareAndUpdate(content.getAnnotations().keySet(), maxAnnotationIndex);
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
