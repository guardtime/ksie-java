package com.guardtime.container.packaging.zip;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.packaging.parsing.ContainerReader;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.parsing.handler.ContentHandler;
import com.guardtime.container.packaging.parsing.store.ParsingStoreException;
import com.guardtime.container.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.container.signature.SignatureFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for reading ZIP container.
 */
class ZipContainerReader extends ContainerReader {

    ZipContainerReader(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory, ParsingStoreFactory storeFactory) throws IOException {
        super(manifestFactory, signatureFactory, storeFactory);
    }

    protected ZipContainerWriter getWriter() {
        return new ZipContainerWriter();
    }

    protected void parseInputStream(InputStream input, HandlerSet handlerSet, ContainerReadingException readingException) throws IOException {
        try (ZipInputStream zipInput = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    LOGGER.trace("Skipping ZIP directory '{}'", entry.getName());
                    continue;
                }
                try {
                    readEntry(zipInput, entry, handlerSet);
                } catch (ParsingStoreException e) {
                    readingException.addException(e);
                }
            }
        }
    }

    private void readEntry(ZipInputStream zipInput, ZipEntry entry, HandlerSet handlerSet) throws ParsingStoreException {
        String name = entry.getName();
        for (ContentHandler handler : handlerSet.getHandlers()) {
            if (handler.isSupported(name)) {
                LOGGER.debug("Reading ZIP entry '{}'. Using handler '{}' ", name, handler.getClass().getName());
                handler.add(name, zipInput);
                return;
            }
        }
        handlerSet.getUnknownFileHandler().add(name, zipInput);
    }

}