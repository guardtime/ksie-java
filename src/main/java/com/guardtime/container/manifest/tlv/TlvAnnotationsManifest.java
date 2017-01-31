package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TlvAnnotationsManifest extends AbstractTlvManifestStructure implements AnnotationsManifest {

    private static final byte[] MAGIC = "KSIEANMF".getBytes(StandardCharsets.UTF_8);

    private List<TlvSingleAnnotationManifestReference> singleAnnotationManifestReferences = new LinkedList<>();

    public TlvAnnotationsManifest(Map<String, Pair<ContainerAnnotation, TlvSingleAnnotationManifest>> singleAnnotationManifests, HashAlgorithmProvider algorithmProvider) throws InvalidManifestException {
        super(MAGIC);
        try {
            Set<String> uris = singleAnnotationManifests.keySet();
            for (String uri : uris) {
                Pair<ContainerAnnotation, TlvSingleAnnotationManifest> pair = singleAnnotationManifests.get(uri);
                this.singleAnnotationManifestReferences.add(new TlvSingleAnnotationManifestReference(uri, pair.getRight(), pair.getLeft().getAnnotationType(), algorithmProvider));
            }
        } catch (TLVParserException | DataHashException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvAnnotationsManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse TlvAnnotationsManifest from InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            switch (element.getType()) {
                case TlvSingleAnnotationManifestReference.ANNOTATION_INFO_REFERENCE:
                    this.singleAnnotationManifestReferences.add(new TlvSingleAnnotationManifestReference(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    @Override
    protected List<TlvSingleAnnotationManifestReference> getElements() {
        return singleAnnotationManifestReferences;
    }

    @Override
    public List<? extends FileReference> getSingleAnnotationManifestReferences() {
        return singleAnnotationManifestReferences;
    }

}
