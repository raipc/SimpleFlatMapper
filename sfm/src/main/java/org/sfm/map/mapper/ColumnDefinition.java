package org.sfm.map.mapper;


import org.sfm.map.FieldKey;
import org.sfm.map.column.ColumnProperty;
import org.sfm.map.column.IgnoreProperty;
import org.sfm.map.column.KeyProperty;
import org.sfm.map.column.RenameProperty;
import org.sfm.reflect.meta.PropertyMeta;
import org.sfm.utils.Predicate;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.sfm.utils.Asserts.requireNonNull;

public abstract class ColumnDefinition<K extends FieldKey<K>, CD extends ColumnDefinition<K, CD>> {

    public static final Predicate<PropertyMeta<?, ?>> DEFAULT_APPLIES_TO = new Predicate<PropertyMeta<?, ?>>() {
        @Override
        public boolean test(PropertyMeta<?, ?> propertyMeta) {
            return false;
        }
    };
    private final ColumnProperty[] properties;

    protected ColumnDefinition(ColumnProperty[] properties) {
        this.properties = requireNonNull("properties", properties);
    }


    public K rename(K key) {
        RenameProperty rp = lookFor(RenameProperty.class);
        if (rp != null) {
            return key.alias(rp.getName());
        }
        return key;
    }

    public boolean ignore() {
        return has(IgnoreProperty.class);
    }

    public boolean has(Class<? extends ColumnProperty> clazz) {
        return lookFor(clazz) != null;
    }

    public boolean isKey() {
        return has(KeyProperty.class);
    }

    public Predicate<PropertyMeta<?, ?>> keyAppliesTo() {
        KeyProperty kp = lookFor(KeyProperty.class);

        if (kp != null) {
            return kp.getAppliesTo();
        }

        return DEFAULT_APPLIES_TO;
    }

    public CD compose(CD columnDefinition) {
        ColumnDefinition cdi = requireNonNull("columnDefinition", columnDefinition);
        ColumnProperty[] properties = Arrays.copyOf(cdi.properties, this.properties.length + cdi.properties.length);
        System.arraycopy(this.properties, 0, properties, cdi.properties.length, this.properties.length);
        return newColumnDefinition(properties);
    }

    public CD add(ColumnProperty... props) {
        requireNonNull("properties", props);
        ColumnProperty[] properties = Arrays.copyOf(this.properties, this.properties.length + props.length);
        System.arraycopy(props, 0, properties, this.properties.length, props.length);
        return newColumnDefinition(properties);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookFor(Class<T> propClass) {
        for(ColumnProperty cp : properties) {
            if (cp != null && propClass.isInstance(cp)) {
                return (T) cp;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] lookForAll(Class<T> propClass) {
        List<T> list = new ArrayList<T>();
        for(ColumnProperty cp : properties) {
            if (cp != null && propClass.isInstance(cp)) {
                list.add((T) cp);
            }
        }
        return list.toArray((T[]) Array.newInstance(propClass, 0));
    }

    protected abstract CD newColumnDefinition(ColumnProperty[] properties);

    public CD addRename(String name) {
        return add(new RenameProperty(name));
    }

    public CD addIgnore() {
        return add(new IgnoreProperty());
    }

    public CD addKey() {
        return add(new KeyProperty());
    }

    public CD addKey(Predicate<PropertyMeta<?, ?>> appliesTo) {
        return add(new KeyProperty(appliesTo));
    }

    protected void appendToStringBuilder(StringBuilder sb) {
        for (int i = 0; i < properties.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(properties[i].toString());
        }
    }

    public String toString() {
        StringBuilder sb  = new StringBuilder();

        sb.append("ColumnDefinition{");
        appendToStringBuilder(sb);
        sb.append("}");

        return sb.toString();
    }

    public abstract boolean hasCustomSource();
    public abstract Type getCustomSourceReturnType();
}
