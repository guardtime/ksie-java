package com.guardtime.container.packaging;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.InvalidPackageException;

import java.util.Collection;
import java.util.Vector;

public class ContainerReadingException extends InvalidPackageException {
    private final Vector<Throwable> exceptions = new Vector<>();
    private Container container;

    public ContainerReadingException(String message) {
        super(message);
    }

    public Vector<Throwable> getExceptions() {
        return exceptions;
    }

    public void addException(Throwable t) {
        exceptions.add(t);
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void addExceptions(Collection<Throwable> throwables) {
        exceptions.addAll(throwables);
    }
}
