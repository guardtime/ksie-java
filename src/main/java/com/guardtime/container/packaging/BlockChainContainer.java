package com.guardtime.container.packaging;

import com.guardtime.container.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface BlockChainContainer {

    /**
     *
     * @return Ordered list of SignatureContent where the order is ascending based on the index of manifest
     */
    List<? extends SignatureContent> getSignatureContents();

    void writeTo(OutputStream output) throws IOException;

    MimeType getMimeType();

    List<Pair<String, File>> getUnknownFiles();

}
