package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;

import java.util.List;

class ZipSignatureContent extends SignatureContent {

    private ZipSignatureContent(List<ContainerDocument> documents,
                                List<Pair<String, ContainerAnnotation>> annotations,
                                Pair<String, DocumentsManifest> documentsManifest,
                                Pair<String, AnnotationsManifest> annotationsManifest,
                                Pair<String, Manifest> manifest,
                                List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestMap) {
        super(documents, annotations, documentsManifest, annotationsManifest, manifest, singleAnnotationManifestMap);
    }

    protected void setSignature(ContainerSignature signature) {
        this.signature = signature;
    }

    public static class Builder extends SignatureContent.Builder {

        @Override
        public ZipSignatureContent build() {
            return new ZipSignatureContent(documents, annotations, documentsManifest, annotationsManifest, manifest, singleAnnotationManifests);
        }
    }

}
