package net.nemezanevem.gregtech.api.unification.material.properties;

import net.nemezanevem.gregtech.api.util.Util;

public class PropertyKey<T extends IMaterialProperty<T>> {

    private final Class<T> type;

    public PropertyKey(Class<T> type) {
        this.type = type;
    }

    protected T constructDefault() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public T cast(IMaterialProperty<?> property) {
        return this.type.cast(property);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PropertyKey key) {
            return Util.getId(key).equals(Util.getId(this));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Util.getId(this).hashCode();
    }

    @Override
    public String toString() {
        return Util.getId(this).toString();
    }
}
