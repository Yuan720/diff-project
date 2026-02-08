package com.github;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class DiffMapBuilder {

    public static <T> void buildForList(
            List<ItemView<T>> views,
            Map<Object, T> oldMap,
            Map<Object, T> newMap,
            Function<T, Object> uniqGetter,
            Map<String, DiffMeta> diffMap
    ) {
        for (ItemView<T> view : views) {
            Object key = uniqGetter.apply(view.getData());
            T oldVal = oldMap.get(key);
            T newVal = newMap.get(key);

            DiffMeta meta = new DiffMeta();

            if (oldVal == null) {
                meta.setChangeType("ADD");
                meta.setFields(extractCompareFields(newVal));
                meta.setOriginValues(null);
            } else if (newVal == null) {
                meta.setChangeType("DELETE");
                meta.setFields(extractCompareFields(oldVal));
                meta.setOriginValues(oldVal);
            } else {
                List<String> changed = diffFields(oldVal, newVal);
                if (changed.isEmpty()) {
                    meta.setChangeType("EQUAL");
                    meta.setFields(null);
                } else {
                    meta.setChangeType("MODIFY");
                    meta.setFields(changed);
                }
                meta.setOriginValues(oldVal);
            }

            diffMap.put(view.getDiffKey(), meta);
        }
    }

    public static List<String> diffFields(Object oldObj, Object newObj) {
        List<String> changed = new ArrayList<>();
        for (Field f : oldObj.getClass().getDeclaredFields()) {
            if (!f.isAnnotationPresent(CompareField.class)) continue;
            f.setAccessible(true);
            try {
                if (!Objects.equals(f.get(oldObj), f.get(newObj))) {
                    changed.add(f.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return changed;
    }

    private static List<String> extractCompareFields(Object obj) {
        List<String> fields = new ArrayList<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(CompareField.class)) {
                fields.add(f.getName());
            }
        }
        return fields;
    }
}
