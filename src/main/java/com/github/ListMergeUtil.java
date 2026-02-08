package com.github;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListMergeUtil {

    public static <T> List<ItemView<T>> merge(
            List<T> oldList,
            List<T> newList,
            Function<T, Object> uniqGetter,
            Function<T, String> diffKeyGen
    ) {
        oldList = oldList != null ? oldList : new ArrayList<>();
        newList = newList != null ? newList : new ArrayList<>();

        // Final "Historical Priority" Algorithm (Git-like)
        // 1. Build the skeleton from oldList, preserving its order.
        List<ItemView<T>> result = new ArrayList<>();
        Map<Object, T> newMap = newList.stream().collect(Collectors.toMap(uniqGetter, Function.identity()));
        Set<Object> oldKeys = oldList.stream().map(uniqGetter).collect(Collectors.toSet());

        for (T oldItem : oldList) {
            Object oldKey = uniqGetter.apply(oldItem);
            T newItem = newMap.get(oldKey);
            // Use the new item's data if it exists, otherwise use the old item's data (for deleted items)
            T data = (newItem != null) ? newItem : oldItem;
            result.add(new ItemView<>(diffKeyGen.apply(data), null, data));
        }

        // 2. Find and insert purely new items into the skeleton.
        List<T> purelyNewItems = newList.stream()
                .filter(newItem -> !oldKeys.contains(uniqGetter.apply(newItem)))
                .collect(Collectors.toList());

        for (T newItem : purelyNewItems) {
            int newItemIndexInNewList = findDataIndex(newList, uniqGetter.apply(newItem), uniqGetter);

            // Find the first subsequent item in newList that is an old item (our anchor).
            Object anchorKey = null;
            for (int i = newItemIndexInNewList + 1; i < newList.size(); i++) {
                Object subsequentKey = uniqGetter.apply(newList.get(i));
                if (oldKeys.contains(subsequentKey)) {
                    anchorKey = subsequentKey;
                    break;
                }
            }

            ItemView<T> newView = new ItemView<>(diffKeyGen.apply(newItem), null, newItem);
            if (anchorKey != null) {
                // Insert the new item before its anchor in the result list.
                int anchorIndexInResult = indexOf(result, anchorKey, uniqGetter);
                if (anchorIndexInResult != -1) {
                    result.add(anchorIndexInResult, newView);
                } else {
                    result.add(newView); // Fallback
                }
            } else {
                // No anchor found, means all items after this are also new. Append to end.
                result.add(newView);
            }
        }

        return result;
    }

    private static <T> int findDataIndex(List<T> list, Object key, Function<T, Object> uniqGetter) {
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(uniqGetter.apply(list.get(i)), key)) {
                return i;
            }
        }
        return -1;
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
