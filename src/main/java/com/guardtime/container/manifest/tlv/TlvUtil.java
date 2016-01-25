package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class TlvUtil {

    public static InputStream generateInputStream(byte[] magicBytes, TLVElement... elements) throws BlockChainContainerException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(magicBytes);
            for(TLVElement element : elements){
                element.writeTo(bos);
            }
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }
}
