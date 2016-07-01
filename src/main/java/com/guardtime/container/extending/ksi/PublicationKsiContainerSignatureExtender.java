package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.ContainerSignatureExtender;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;

/**
 * Extends all {@link KSISignature}s in {@link com.guardtime.container.packaging.Container} to specified publication
 * record. The publication time of the publication record must be after signature aggregation time.
 */
public class PublicationKsiContainerSignatureExtender extends ContainerSignatureExtender<KSISignature> {
    private final PublicationRecord publicationRecord;
    protected final KSI ksi;

    public PublicationKsiContainerSignatureExtender(KSI ksi, PublicationRecord publicationRecord) {
        Util.notNull(ksi, "KSI");
        Util.notNull(publicationRecord, "Publication record");
        this.ksi = ksi;
        this.publicationRecord = publicationRecord;
    }

    @Override
    protected boolean unsupportedContainerSignature(ContainerSignature containerSignature) {
        return !(containerSignature.getSignature() instanceof KSISignature);
    }

    @Override
    protected KSISignature getExtendedSignature(ContainerSignature containerSignature) throws SignatureException {
        try {
            KSISignature ksiSignature = (KSISignature) containerSignature.getSignature();
            return ksi.extend(ksiSignature, publicationRecord);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

}
