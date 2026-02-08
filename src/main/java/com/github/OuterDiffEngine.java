package com.github;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OuterDiffEngine {

    public static OuterView build(Outer oldObj, Outer newObj) {

        OuterView view = new OuterView();
        view.setValue1(newObj.getValue1());
        view.setValue2(newObj.getValue2());
        view.setValue3(newObj.getValue3());

        String rootDiffKey = DiffKeyGenerator.next();
        view.setDiffKey(rootDiffKey);

        Map<String, DiffMeta> diffMap = new LinkedHashMap<>();

        // root diff
        DiffMeta rootMeta = new DiffMeta();
        List<String> rootChanged = DiffMapBuilder
                .diffFields(oldObj, newObj);
        rootMeta.setChangeType(
                rootChanged.isEmpty() ? "EQUAL" : "MODIFY");
        rootMeta.setFields(rootChanged);
        Outer oldObjWithoutItems = new Outer();
        if (oldObj != null) { // Add a null check for oldObj
            oldObjWithoutItems.setValue1(oldObj.getValue1());
            oldObjWithoutItems.setValue2(oldObj.getValue2());
            oldObjWithoutItems.setValue3(oldObj.getValue3());
        }
        rootMeta.setOriginValues(oldObjWithoutItems);
        diffMap.put(rootDiffKey, rootMeta);

        // ===== items =====
        Function<Inner, Object> uniqGetter = i -> {
            try {
                for (Field f : Inner.class.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Uniq.class)) {
                        f.setAccessible(true);
                        return f.get(i);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            throw new IllegalStateException("No @Uniq field");
        };

        List<ItemView<Inner>> mergedItems =
                ListMergeUtil.merge(
                        oldObj.getItems(),
                        newObj.getItems(),
                        uniqGetter,
                        i -> DiffKeyGenerator.next()
                );

        view.setItems(mergedItems);

        Map<Object, Inner> oldMap = mapByUniq(oldObj.getItems(), uniqGetter);
        Map<Object, Inner> newMap = mapByUniq(newObj.getItems(), uniqGetter);

        DiffMapBuilder.buildForList(
                mergedItems,
                oldMap,
                newMap,
                uniqGetter,
                diffMap
        );

        view.setDiffMap(diffMap);
        view.setOldValue(oldObj);
        return view;
    }

    private static <T> Map<Object, T> mapByUniq(
            List<T> list,
            Function<T, Object> uniqGetter
    ) {
        Map<Object, T> map = new LinkedHashMap<>();
        if (list == null) return map;
        for (T t : list) {
            map.put(uniqGetter.apply(t), t);
        }
        return map;
    }
}
