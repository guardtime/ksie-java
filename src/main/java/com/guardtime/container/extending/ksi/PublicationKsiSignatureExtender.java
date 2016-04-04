package com.guardtime.container.extending.ksi;

import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;

public class PublicationKsiSignatureExtender extends KsiSignatureExtender {
    private final PublicationRecord publicationRecord;

    public PublicationKsiSignatureExtender(KSI ksi, PublicationRecord publicationRecord) {
        super(ksi);
        Util.notNull(publicationRecord, "PublicationRecord");
        this.publicationRecord = publicationRecord;
    }

    @Override
    protected KsiContainerSignature getExtendedSignature(KSISignature extendableSignature) throws KSIException {
        return new KsiContainerSignature(ksi.extend(extendableSignature, publicationRecord));
    }
}
