package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.util.Pair;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class RandomTest extends AbstractCommonKsiServiceIntegrationTest {

    @Test
    public void testCreateContainer() throws Exception {
        // TODO: Create container as per requirement
//        Container container = packagingFactory.read(new FileInputStream(loadFile("containers/container-no-document-uri-in-manifest.ksie")));
//        container.getSignatureContents();

        Container container = new ContainerBuilder(packagingFactory).
                withDocument(loadFile("test-data-files/test.txt"), "text/plain").
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.VALUE_REMOVABLE, "someAnnotation", "some.key")).
                build();
        container.writeTo(new FileOutputStream("src/test/resources/containers/new_container-no-document-uri-in-manifest.ksie"));


//        Container container = new ContainerBuilder(packagingFactory).
//        withDocument(loadFile("test-data-files/test.txt"), "test/plain").
//        build();
//        container.writeTo(new FileOutputStream("src/test/resources/containers/new_container-one-document.ksie"));
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        Container container = new ContainerBuilder(packagingFactory).
//                withDocument(loadFile("test-data-files/test.txt"), "text/plain").
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, "someAnnotation2", "some.key")).
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.VALUE_REMOVABLE, "someAnnotation", "some.key")).
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.VALUE_REMOVABLE, "someAnnotation3", "some.key")).
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, "someAnnotation4", "some.key")).
//                build();
//        container.writeTo(new FileOutputStream("src/test/resources/containers/new_container-invalid-annotation-type.ksie"));
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        Container container = new ContainerBuilder(packagingFactory).
//                withDocument(loadFile("test-data-files/test.txt"), "text/plain").
//                build();
//        container.getUnknownFiles().add(Pair.of("sun.txt", loadFile("test-data-files/test.txt")));
//        container.getUnknownFiles().add(Pair.of("META-INF/sun.txt", loadFile("test-data-files/test.txt")));
//        container.writeTo(new FileOutputStream("src/test/resources/containers/new_container-unkown-files.ksie"));
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        Container container = new ContainerBuilder(packagingFactory).
//                withDocument(loadFile("test-data-files/test.txt"), "text/plain").
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, "someAnnotation2", "some.key")).
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, "someAnnotation", "some.key")).
//                build();
//
//        container = new ContainerBuilder(packagingFactory).
//                withDocument(new FileInputStream(loadFile("test-data-files/test.txt")), "data2.txt", "text/plain").
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, "someAnnotation3", "some.key")).
//                withExistingContainer(container).
//        build();
//
//        container = new ContainerBuilder(packagingFactory).
//                withDocument(new FileInputStream(loadFile("test-data-files/test.txt")), "data3.txt", "text/plain").
//                withAnnotation(new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, "someAnnotation4", "some.key")).
//                withExistingContainer(container).
//        build();
//        container.writeTo(new FileOutputStream("src/test/resources/containers/new_container-broken-signature.ksie"));
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        Container container = new ContainerBuilder(packagingFactory).
//        withDocument(loadFile("test-data-files/test.txt"), null).
//        build();
//        container.writeTo(new FileOutputStream("src/test/resources/containers/new_container-document-missing-mimetype.ksie"));
    }
}
