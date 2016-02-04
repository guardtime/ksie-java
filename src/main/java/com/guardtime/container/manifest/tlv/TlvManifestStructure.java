package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.*;
import java.util.*;

public abstract class TlvManifestStructure {

    private Set<Integer> processedElements = new HashSet<>();
    private String uri;

    public TlvManifestStructure(String uri) {
        this.uri = uri;
    }

    protected List<TLVElement> parseElementsFromStream(InputStream stream) throws TLVParserException {
        if (stream == null) {
            throw new TLVParserException("Stream must be present");
        }
        try {
            byte[] magic = new byte[8]; // TODO: Fix byte array size
            if (stream.read(magic) == 0) {
                throw new TLVParserException("Stream must contain data");
            }

            if (!Arrays.equals(magic, getMagic())) {
                throw new TLVParserException("Invalid magic for manifest type");
            }

            List<TLVElement> elements = new LinkedList<>();
            TLVElement elem;

            TLVInputStream tlvStream;
            if (stream instanceof TLVInputStream) {
                tlvStream = (TLVInputStream) stream;
            } else {
                tlvStream = new TLVInputStream(stream);
            }
            while (tlvStream.hasNextElement()) {
                elem = tlvStream.readElement();
                elements.add(elem);
            }
            return elements;
        } catch (IOException e) {
            throw new TLVParserException("Failed to read stream", e);
        }
    }

    /**
     * Checks that TLV element is critical or not.
     *
     * @param element
     *         element to check
     * @throws TLVParserException
     *         when unknown critical TLV element is encountered.
     */
    protected void verifyCriticalFlag(TLVElement element) throws TLVParserException {
        if (!element.isNonCritical()) {
            throw new TLVParserException("Unknown critical TLV element with tag=0x" + Integer.toHexString(element.getType()) + " encountered");
        }
    }

    /**
     * @param element
     *         - element of type to read only once.
     * @return instance of {@link TLVElement}
     * @throws TLVParserException
     *         when TLV element with type is already processed.
     */
    protected TLVElement readOnce(TLVElement element) throws TLVParserException {
        int tlvElementType = element.getType();
        if (!processedElements.contains(tlvElementType)) {
            processedElements.add(tlvElementType);
            return element;
        }
        throw new TLVParserException("Multiple TLV 0x" + Integer.toHexString(tlvElementType) + " elements. Only one is allowed.");
    }

    protected abstract byte[] getMagic();

    protected abstract List<TLVElement> getElements();

    public String getUri() {
        return uri;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (out == null) {
            throw new IOException("Output stream can not be null");
        }
        try {
            out.write(getMagic());
            for (TLVElement elem : getElements()) {
                elem.writeTo(out);
            }
        } catch (TLVParserException e) {
            throw new IOException("Writing to OutputStream failed", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlvManifestStructure that = (TlvManifestStructure) o;
        if (!this.getMagic().equals(that.getMagic())) return false;
        if (that.getElements() == null) return false;
        return this.getElements().equals(that.getElements());
    }

    @Override
    public int hashCode() {
        int code = 1;
        code += getMagic().hashCode();
        for (TLVElement element : getElements()) {
            code += element.hashCode();
        }
        return code;
    }

    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeTo(bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }
}