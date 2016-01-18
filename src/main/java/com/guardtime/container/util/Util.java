package com.guardtime.container.util;

import com.guardtime.container.datafile.ContainerDataFile;

import java.util.Collection;

public final class Util {

    public static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name + " must be present");
        }
    }

    public static void notEmpty(Collection<?> o, String name) {
        notNull(o, name);
        if (o.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }


    private Util() {
    }

}
