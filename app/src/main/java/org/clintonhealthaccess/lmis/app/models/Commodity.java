package org.clintonhealthaccess.lmis.app.models;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

public class Commodity implements Serializable {
    private final String id;
    private final String name;

    public Commodity(String name) {
        this.id = name;
        this.name = name;
    }

    static List<Commodity> buildList(String[] commodityNames) {
        return copyOf(transform(copyOf(commodityNames), new Function<String, Commodity>() {
            @Override
            public Commodity apply(String name) {
                return new Commodity(name);
            }
        }));
    }

    public String getName() {
        return name;
    }
}
