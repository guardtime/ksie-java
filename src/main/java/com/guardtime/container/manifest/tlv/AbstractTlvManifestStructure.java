package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class AbstractTlvManifestStructure {

    private Set<Integer> processedElements = new HashSet<>();
    private byte[] magic;

    public AbstractTlvManifestStructure(byte[] magic) {
        Util.notNull(magic, "Magic bytes");
        this.magic = magic;
    }

    public AbstractTlvManifestStructure(byte[] magic, InputStream stream) throws InvalidManifestException {
        this(magic);
        try {
            Util.notNull(stream, "InputStream");
            byte[] inputStreamMagicBytes = new byte[magic.length];
            if (stream.read(inputStreamMagicBytes) == 0) {
                throw new InvalidManifestException("Stream must contain data");
            }
            if (!Arrays.equals(inputStreamMagicBytes, magic)) {
                throw new InvalidManifestException("Invalid magic for manifest type");
            }
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read stream", e);
        }
    }

    TLVInputStream toTlvInputStream(InputStream input) throws TLVParserException {
        if (input instanceof TLVInputStream) {
            return (TLVInputStream) input;
        }
        // TODO remove exception when SDK is fixed.
        return new TLVInputStream(input);
    }

    /**
     * Checks that TLV element is critical or not.
     *
     * @param element element to check
     * @throws TLVParserException when unknown critical TLV element is encountered.
     */
    protected void verifyCriticalFlag(TLVElement element) throws TLVParserException {
        if (!element.isNonCritical()) {
            throw new TLVParserException("Unknown critical TLV element with tag=0x" + Integer.toHexString(element.getType()) + " encountered");
        }
    }

    /**
     * @param element - element of type to read only once.
     * @return instance of {@link TLVElement}
     * @throws TLVParserException when TLV element with type is already processed.
     */
    protected TLVElement readOnce(TLVElement element) throws TLVParserException {
        int tlvElementType = element.getType();
        if (!processedElements.contains(tlvElementType)) {
            processedElements.add(tlvElementType);
            return element;
        }
        throw new TLVParserException("Multiple TLV 0x" + Integer.toHexString(tlvElementType) + " elements. Only one is allowed.");
    }

    protected void checkMandatoryElement(TLVStructure structure, String name) throws InvalidManifestException {
        if (structure == null) {
            throw new InvalidManifestException(name + " is mandatory manifest element");
        }
    }

    protected abstract List<? extends TLVStructure> getElements();

    public byte[] getMagic() {
        return magic;
    }

    public void writeTo(OutputStream out) throws IOException {
        Util.notNull(out, "Output stream");
        try {
            out.write(getMagic());
            for (TLVStructure elem : getElements()) {
                elem.writeTo(out);
            }
        } catch (KSIException e) {
            throw new IOException("Writing to OutputStream failed", e);
        }
    }

    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeTo(bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTlvManifestStructure that = (AbstractTlvManifestStructure) o;
        if (!Arrays.equals(this.getMagic(), that.getMagic())) return false;
        if (this.getElements() == null) {
            return that.getElements() == null;
        }
        if (that.getElements() == null) return false;
        return this.getElements().equals(that.getElements());
    }

    @Override
    public int hashCode() {
        int code = 1;
        code += Arrays.hashCode(getMagic());
        for (TLVStructure element : getElements()) {
            code += element.hashCode();
        }
        return code;
    }

}