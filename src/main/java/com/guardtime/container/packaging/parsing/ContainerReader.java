package com.guardtime.container.packaging.parsing;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerWriter;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.MimeTypeEntry;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.exception.InvalidPackageException;
import com.guardtime.container.packaging.parsing.handler.AnnotationContentHandler;
import com.guardtime.container.packaging.parsing.handler.AnnotationsManifestHandler;
import com.guardtime.container.packaging.parsing.handler.ContentHandler;
import com.guardtime.container.packaging.parsing.handler.ContentParsingException;
import com.guardtime.container.packaging.parsing.handler.DocumentContentHandler;
import com.guardtime.container.packaging.parsing.handler.DocumentsManifestHandler;
import com.guardtime.container.packaging.parsing.handler.ManifestHandler;
import com.guardtime.container.packaging.parsing.handler.MimeTypeHandler;
import com.guardtime.container.packaging.parsing.handler.SignatureHandler;
import com.guardtime.container.packaging.parsing.handler.SingleAnnotationManifestHandler;
import com.guardtime.container.packaging.parsing.handler.UnknownFileHandler;
import com.guardtime.container.packaging.parsing.store.ParsingStore;
import com.guardtime.container.packaging.parsing.store.ParsingStoreException;
import com.guardtime.container.packaging.parsing.store.ParsingStoreFactory;
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

import static com.guardtime.container.packaging.MimeType.MIME_TYPE_ENTRY_NAME;

/**
 * Provides stream parsing logic for {@link com.guardtime.container.packaging.ContainerPackagingFactory}
 * Derivatives of this class must be stateless and reusable.
 */
public abstract class ContainerReader {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ContainerReader.class);

    private final ContainerManifestFactory manifestFactory;
    private final SignatureFactory signatureFactory;
    private final ParsingStoreFactory parsingStoreFactory;

    public ContainerReader(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory, ParsingStoreFactory storeFactory) throws IOException {
        Util.notNull(manifestFactory, "Manifest factory");
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(storeFactory, "Parsing store factory");
        this.manifestFactory = manifestFactory;
        this.signatureFactory = signatureFactory;
        this.parsingStoreFactory = storeFactory;
    }

    public Container read(InputStream input) throws IOException, ContainerReadingException, ParsingStoreException {
        ContainerReadingException readingException = new ContainerReadingException("Reading container encountered errors!");
        ParsingStore parsingStore = parsingStoreFactory.create();
        HandlerSet handlerSet = new HandlerSet(manifestFactory, signatureFactory, parsingStore);
        parseInputStream(input, handlerSet, readingException);
        List<SignatureContent> contents = buildSignatures(handlerSet, readingException);
        MimeType mimeType = getMimeType(handlerSet);
        List<UnknownDocument> unknownFiles = getUnknownFiles(handlerSet, readingException);
        handlerSet.clearRequestedData();
        Container container = new Container(contents, unknownFiles, mimeType, getWriter(), parsingStore);
        readingException.setContainer(container);

        if (!validMimeType(mimeType) || !containsValidContents(contents)) {
            readingException.addException(new InvalidPackageException("Parsed container was not valid"));
        }

        if (!readingException.getExceptions().isEmpty()) {
            throw readingException;
        }
        return container;
    }

    protected abstract ContainerWriter getWriter();

    protected abstract void parseInputStream(InputStream input, HandlerSet handlerSet, ContainerReadingException readingException) throws IOException;

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

    private MimeType getMimeType(HandlerSet handlerSet) {
        try {
            String uri = MIME_TYPE_ENTRY_NAME;
            byte[] content = handlerSet.mimeTypeHandler.get(uri);
            return new MimeTypeEntry(uri, content);
        } catch (ContentParsingException e) {
            LOGGER.debug("Failed to parse MIME type. Reason: '{}", e.getMessage());
            return null;
        }
    }

    private List<UnknownDocument> getUnknownFiles(HandlerSet handlerSet, ContainerReadingException readingException) {
        List<UnknownDocument> returnable = new LinkedList<>();
        try {
            returnable.addAll(handlerSet.unknownFileHandler.getUnrequestedFiles());
        } catch (ParsingStoreException e) {
            readingException.addException(e);
        }
        for (ContentHandler handler : handlerSet.handlers) {
            try {
                returnable.addAll(handler.getUnrequestedFiles());
            } catch (ParsingStoreException e) {
                readingException.addException(e);
            }
        }
        return returnable;
    }

    private List<SignatureContent> buildSignatures(HandlerSet handlerSet, ContainerReadingException readingException) {
        Set<String> parsedManifestUriSet = handlerSet.manifestHandler.getNames();
        SignatureContentHandler signatureContentHandler = new SignatureContentHandler(
                handlerSet.documentHandler,
                handlerSet.annotationContentHandler,
                handlerSet.manifestHandler,
                handlerSet.documentsManifestHandler,
                handlerSet.annotationsManifestHandler,
                handlerSet.singleAnnotationManifestHandler,
                handlerSet.signatureHandler
        );
        List<SignatureContent> signatures = new LinkedList<>();
        for (String manifestUri : parsedManifestUriSet) {
            try {
                Pair<SignatureContent, List<Throwable>> signatureContentVectorPair = signatureContentHandler.get(manifestUri, parsingStoreFactory);
                signatures.add(signatureContentVectorPair.getLeft());
                readingException.addExceptions(signatureContentVectorPair.getRight());
            } catch (ContentParsingException | ParsingStoreException e) {
                LOGGER.debug("Parsing SignatureContent failed for '{}'. Reason: '{}'", manifestUri, e.getMessage());
                readingException.addException(e);
            }
        }
        return signatures;
    }

    protected class HandlerSet {
        private final DocumentContentHandler documentHandler;
        private final AnnotationContentHandler annotationContentHandler;
        private final UnknownFileHandler unknownFileHandler;
        private final MimeTypeHandler mimeTypeHandler;
        private final ManifestHandler manifestHandler;
        private final DocumentsManifestHandler documentsManifestHandler;
        private final AnnotationsManifestHandler annotationsManifestHandler;
        private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
        private final SignatureHandler signatureHandler;

        private ContentHandler[] handlers;

        HandlerSet(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory, ParsingStore store) {
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
        }

        public ContentHandler[] getHandlers() {
            return handlers;
        }

        public UnknownFileHandler getUnknownFileHandler() {
            return unknownFileHandler;
        }

        public void clearRequestedData() {
            for(ContentHandler handler : handlers) {
                handler.clearRequestedData();
            }
        }
    }

}
