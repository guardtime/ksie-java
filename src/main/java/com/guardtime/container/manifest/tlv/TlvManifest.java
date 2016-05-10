package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

//TODO fix reference object
class TlvManifest extends AbstractTlvManifestStructure implements Manifest {

    private static final byte[] MAGIC = "KSIEMFST".getBytes();

    private TlvDataFilesManifestReference dataFilesManifestReference;
    private TlvSignatureReference signatureReference;
    private TlvAnnotationsManifestReference annotationsManifestReference;

    public TlvManifest(Pair<String, TlvDataFilesManifest> dataFilesManifest, Pair<String, TlvAnnotationsManifest> annotationsManifest, Pair<String, String> signatureReference) throws InvalidManifestException {
        super(MAGIC);
        try {
            this.dataFilesManifestReference = new TlvDataFilesManifestReference(dataFilesManifest.getRight(), dataFilesManifest.getLeft());
            this.signatureReference = new TlvSignatureReference(signatureReference.getLeft(), signatureReference.getRight());
            this.annotationsManifestReference = new TlvAnnotationsManifestReference(annotationsManifest.getLeft(), annotationsManifest.getRight());
        } catch (TLVParserException | IOException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            TLVInputStream inputStream = toTlvInputStream(stream);
            read(inputStream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse content of InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
        checkMandatoryElement(dataFilesManifestReference, "Data files manifest reference");
        checkMandatoryElement(signatureReference, "Signature reference");
        checkMandatoryElement(annotationsManifestReference, "Annotations manifest reference");
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return Util.hash(getInputStream(), algorithm);
    }

    @Override
    public FileReference getDataFilesManifestReference() {
        return dataFilesManifestReference;
    }

    @Override
    public FileReference getAnnotationsManifestReference() {
        return annotationsManifestReference;
    }

    @Override
    public com.guardtime.container.manifest.SignatureReference getSignatureReference() {
        return signatureReference;
    }

    @Override
    protected List<TLVStructure> getElements() {
        return asList(dataFilesManifestReference, signatureReference, annotationsManifestReference);
    }

    //TODO this isn't the best solution
    private void read(TLVInputStream inputStream) throws IOException, TLVParserException {
        TLVElement element;
        while (inputStream.hasNextElement()) {
            element = inputStream.readElement();
            switch (element.getType()) {
                case TlvDataFilesManifestReference.DATA_FILES_MANIFEST_REFERENCE:
                    dataFilesManifestReference = new TlvDataFilesManifestReference(readOnce(element));
                    break;
                case TlvSignatureReference.SIGNATURE_REFERENCE:
                    signatureReference = new TlvSignatureReference(readOnce(element));
                    break;
                case TlvAnnotationsManifestReference.ANNOTATIONS_MANIFEST_REFERENCE:
                    annotationsManifestReference = new TlvAnnotationsManifestReference(readOnce(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

}
