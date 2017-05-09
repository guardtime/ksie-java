package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.parsing.store.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.SINGLE_ANNOTATION_MANIFEST_FORMAT;

/**
 * This content holders is used for annotation manifests inside the container.
 */
public class SingleAnnotationManifestHandler extends ContentHandler<SingleAnnotationManifest> {

    private final ContainerManifestFactory manifestFactory;

    public SingleAnnotationManifestHandler(ContainerManifestFactory manifestFactory, ParsingStore store) {
        super(store);
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(SINGLE_ANNOTATION_MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected SingleAnnotationManifest getEntry(String name) throws ContentParsingException {
        try (InputStream input = fetchStreamFromEntries(name)) {
            SingleAnnotationManifest singleAnnotationManifest = manifestFactory.readSingleAnnotationManifest(input);
            parsingStore.remove(name);
            return singleAnnotationManifest;
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of '" + name + "'", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read content of '" + name + "'", e);
        }
    }

}
