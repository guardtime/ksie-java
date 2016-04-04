package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.SignatureExtender;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;

public class PublicationKsiSignatureExtender implements SignatureExtender {
    private final KSI ksi;
    private final PublicationRecord publicationRecord;

    public PublicationKsiSignatureExtender(KSI ksi, PublicationRecord publicationRecord) {
        this.ksi = ksi;
        this.publicationRecord = publicationRecord;
    }

    @Override
    public ContainerSignature extend(ContainerSignature signature) throws SignatureException {
        try {
            KSISignature extendableSignature = ((KsiContainerSignature) signature).getSignature();
            return new KsiContainerSignature(ksi.extend(extendableSignature, publicationRecord));
        } catch (ClassCastException | KSIException e) {
            throw new SignatureException(e);
        }
    }
}
