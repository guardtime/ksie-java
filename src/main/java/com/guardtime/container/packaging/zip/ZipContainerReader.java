package com.guardtime.container.packaging.zip;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.exception.InvalidPackageException;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.parsing.ParsingStoreException;
import com.guardtime.container.packaging.zip.handler.AnnotationContentHandler;
import com.guardtime.container.packaging.zip.handler.AnnotationsManifestHandler;
import com.guardtime.container.packaging.zip.handler.ContentHandler;
import com.guardtime.container.packaging.zip.handler.ContentParsingException;
import com.guardtime.container.packaging.zip.handler.DocumentContentHandler;
import com.guardtime.container.packaging.zip.handler.DocumentsManifestHandler;
import com.guardtime.container.packaging.zip.handler.ManifestHandler;
import com.guardtime.container.packaging.zip.handler.MimeTypeHandler;
import com.guardtime.container.packaging.zip.handler.SignatureHandler;
import com.guardtime.container.packaging.zip.handler.SingleAnnotationManifestHandler;
import com.guardtime.container.packaging.zip.handler.UnknownFileHandler;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.guardtime.container.packaging.MimeType.MIME_TYPE_ENTRY_NAME;

/**
 * Helper class for reading Zip container.
 * NB! Can only be used once!
 */
class ZipContainerReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipContainerReader.class);

    private final DocumentContentHandler documentHandler;
    private final AnnotationContentHandler annotationContentHandler;
    private final UnknownFileHandler unknownFileHandler;
    private final MimeTypeHandler mimeTypeHandler;
    private final ManifestHandler manifestHandler;
    private final DocumentsManifestHandler documentsManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
    private final SignatureHandler signatureHandler;
    private final SignatureContentHandler signatureContentHandler;
    private final ParsingStore parsingStore;

    private ContentHandler[] handlers;

    ZipContainerReader(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory, ParsingStore store) throws IOException {
        Util.notNull(store, "Parsing store");
        this.parsingStore = store;
        this.documentHandler = new DocumentContentHandler(store);
        this.annotationContentHandler = new AnnotationContentHandler(store);
        this.unknownFileHandler = new UnknownFileHandler(store);
        this.mimeTypeHandler = new MimeTypeHandler(store);
        this.manifestHandler = new ManifestHandler(manifestFactory, store);
        this.documentsManifestHandler = new DocumentsManifestHandler(manifestFactory, store);
        this.annotationsManifestHandler = new AnnotationsManifestHandler(manifestFactory, store);
        this.singleAnnotationManifestHandler = new SingleAnnotationManifestHandler(manifestFactory, store);
        this.signatureHandler = new SignatureHandler(signatureFactory, store);
        this.handlers = new ContentHandler[]{mimeTypeHandler, documentHandler, annotationContentHandler, documentsManifestHandler,
                manifestHandler, annotationsManifestHandler, signatureHandler, singleAnnotationManifestHandler};

        this.signatureContentHandler = new SignatureContentHandler(documentHandler, annotationContentHandler, manifestHandler,
                documentsManifestHandler, annotationsManifestHandler, singleAnnotationManifestHandler, signatureHandler);
    }

    ZipContainer read(InputStream input) throws IOException, ContainerReadingException {
        ContainerReadingException readingException = new ContainerReadingException("Reading container encountered errors!");
        try (ZipInputStream zipInput = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    LOGGER.trace("Skipping directory '{}'", entry.getName());
                    continue;
                }
                try {
                    readEntry(zipInput, entry);
                } catch (ParsingStoreException e) {
                    readingException.addException(e);
                }
            }
        }
        List<SignatureContent> contents = buildSignatures(readingException);
        MimeType mimeType = getMimeType();
        List<UnknownDocument> unknownFiles = getUnknownFiles(readingException);
        ZipContainer zipContainer = new ZipContainer(contents, unknownFiles, mimeType, parsingStore);
        readingException.setContainer(zipContainer);

        if (!validMimeType(mimeType) || !containsValidContents(contents)) {
            readingException.addException(new InvalidPackageException("Parsed container was not valid"));
        }

        if (!readingException.getExceptions().isEmpty()) {
            throw readingException;
        }
        return zipContainer;
    }

    private boolean containsValidContents(List<SignatureContent> signatureContents) {
        for (SignatureContent content : signatureContents) {
            if (containsManifest(content) ||
                    containsOrContainedDocuments(content) ||
                    containsOrContainedAnnotations(content)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOrContainedDocuments(SignatureContent content) {
        return content.getDocuments().size() > 0 || content.getDocumentsManifest().getRight().getDocumentReferences().size() > 0;
    }

    private boolean containsOrContainedAnnotations(SignatureContent content) {
        return content.getAnnotations().size() > 0 || content.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().size() > 0;
    }

    private boolean containsManifest(SignatureContent content) {
        return content.getManifest() != null ||
                content.getDocumentsManifest() != null ||
                content.getAnnotationsManifest() != null;
    }

    private boolean validMimeType(MimeType mimeType) {
        if (mimeType == null) {
            return false;
        }
        try (InputStream inputStream = mimeType.getInputStream()) {
            return inputStream.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private MimeType getMimeType() {
        try {
            String uri = MIME_TYPE_ENTRY_NAME;
            byte[] content = mimeTypeHandler.get(uri);
            return new MimeTypeEntry(uri, content);
        } catch (ContentParsingException e) {
            LOGGER.debug("Failed to parse MIME type. Reason: '{}", e.getMessage());
            return null;
        }
    }

    private List<UnknownDocument> getUnknownFiles(ContainerReadingException readingException) {
        List<UnknownDocument> returnable = new LinkedList<>();
        try {
            returnable.addAll(unknownFileHandler.getUnrequestedFiles());
        } catch (ParsingStoreException e) {
            readingException.addException(e);
        }
        for (ContentHandler handler : handlers) {
            try {
                returnable.addAll(handler.getUnrequestedFiles());
            } catch (ParsingStoreException e) {
                readingException.addException(e);
            }
        }
        return returnable;
    }

    private void readEntry(ZipInputStream zipInput, ZipEntry entry) throws ParsingStoreException {
        String name = entry.getName();
        for (ContentHandler handler : handlers) {
            if (handler.isSupported(name)) {
                LOGGER.debug("Reading zip entry '{}'. Using handler '{}' ", name, handler.getClass().getName());
                handler.add(name, zipInput);
                return;
            }
        }
        unknownFileHandler.add(name, zipInput);
    }

    private List<SignatureContent> buildSignatures(ContainerReadingException readingException) {
        Set<String> parsedManifestUriSet = manifestHandler.getNames();
        List<SignatureContent> signatures = new LinkedList<>();
        for (String manifestUri : parsedManifestUriSet) {
            try {
                Pair<SignatureContent, List<Throwable>> zipSignatureContentVectorPair = signatureContentHandler.get(manifestUri);
                signatures.add(zipSignatureContentVectorPair.getLeft());
                readingException.addExceptions(zipSignatureContentVectorPair.getRight());
            } catch (ContentParsingException e) {
                LOGGER.debug("Parsing SignatureContent failed for '{}'. Reason: '{}'", manifestUri, e.getMessage());
                readingException.addException(e);
            }
        }
        return signatures;
    }

}