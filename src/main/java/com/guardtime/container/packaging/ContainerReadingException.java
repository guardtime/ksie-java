package com.guardtime.container.packaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContainerReadingException extends InvalidPackageException {
    private final List<Throwable> exceptions = new ArrayList<>();
    private Container container;

    public ContainerReadingException(String message) {
        super(message);
    }

    public List<Throwable> getExceptions() {
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
