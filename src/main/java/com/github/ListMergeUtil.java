package com.github;

import java.util.*;
import java.util.function.Function;

public class ListMergeUtil {

    public static <T> List<ItemView<T>> merge(
            List<T> oldList,
            List<T> newList,
            Function<T, Object> uniqGetter,
            Function<T, String> diffKeyGen
    ) {
        oldList = oldList == null ? Collections.emptyList() : oldList;
        newList = newList == null ? Collections.emptyList(): newList;

        List<ItemView<T>> result = new ArrayList<>();

        Map<Object, T> newMap = new LinkedHashMap<>();
        for (T n : newList) {
            newMap.put(uniqGetter.apply(n), n);
        }

        Set<Object> oldKeys = new LinkedHashSet<>();

        // 1. oldList 作为骨架
        for (T o : oldList) {
            Object key = uniqGetter.apply(o);
            oldKeys.add(key);

            T newVal = newMap.get(key);
            if (newVal != null) {
                result.add(new ItemView<>(
                        diffKeyGen.apply(newVal),
                        null,
                        newVal
                ));
            } else {
                result.add(new ItemView<>(
                        diffKeyGen.apply(o),
                        null,
                        o
                ));
            }
        }

        // 2. 新增元素：前驱锚点插入
        for (int i = 0; i < newList.size(); i++) {
            T n = newList.get(i);
            Object key = uniqGetter.apply(n);

            if (oldKeys.contains(key)) continue;

            Object anchorKey = null;
            for (int j = i - 1; j >= 0; j--) {
                Object prevKey = uniqGetter.apply(newList.get(j));
                if (oldKeys.contains(prevKey)) {
                    anchorKey = prevKey;
                    break;
                }
            }

            ItemView<T> view = new ItemView<>(
                    diffKeyGen.apply(n),
                    null,
                    n
            );

            if (anchorKey == null) {
                result.add(view);
            } else {
                int idx = indexOf(result, anchorKey, uniqGetter);
                if (idx == -1) {
                    // Fallback: If anchorKey was expected to be found but wasn't,
                    // add the view to the end of the list.
                    result.add(view);
                } else {
                    result.add(idx + 1, view);
                }
            }
        }

        return result;
    }

    private static <T> int indexOf(
            List<ItemView<T>> list,
            Object key,
            Function<T, Object> uniqGetter
    ) {
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(
                    uniqGetter.apply(list.get(i).getData()),
                    key
            )) {
                return i;
            }
        }
        return -1;
    }
}
