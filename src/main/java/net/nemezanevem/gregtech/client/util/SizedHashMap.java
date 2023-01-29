package net.nemezanevem.gregtech.client.util;

import java.io.Serial;
import java.util.HashMap;

public class SizedHashMap<K, V> extends HashMap<K, V> {

    @Serial
    private static final long serialVersionUID = 1L;
    private final int max;

    public SizedHashMap(int capacity) {
        super();
        max = capacity;
    }

    @Override
    public V put(K key, V value) {
        if (super.size() >= max && !super.containsKey(key)) {
            return null;
        } else {
            super.put(key, value);
            return value;
        }
    }

    public void fill(V value) {
        for (int i = 0; i < max; ++i) {
            this.put
        }
    }
}
