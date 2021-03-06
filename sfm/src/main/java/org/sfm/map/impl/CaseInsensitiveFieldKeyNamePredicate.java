package org.sfm.map.impl;

import org.sfm.map.FieldKey;
import org.sfm.utils.Named;
import org.sfm.utils.Predicate;

public class CaseInsensitiveFieldKeyNamePredicate implements Predicate<FieldKey<?>>, Named {
    private final String name;

    public CaseInsensitiveFieldKeyNamePredicate(String name) {
        this.name = name;
    }

    @Override
    public boolean test(FieldKey<?> fieldKey) {
        return fieldKey.getName().equalsIgnoreCase(name);
    }

    @Override
    public String getName() {
        return name;
    }
}
