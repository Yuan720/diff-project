package com.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DiffMapBuilderTest {

    private Map<String, DiffMeta> diffMap;
    private Inner oldItem;
    private Inner newItem;
    private Inner addedItem;
    private Inner deletedItem;

    @BeforeEach
    void setUp() {
        diffMap = new HashMap<>();
        oldItem = new Inner(1, "name_old", "desc_old", 10);
        newItem = new Inner(1, "name_new", "desc_old", 10); // only name changed
        addedItem = new Inner(2, "name_added", "desc_added", 20);
        deletedItem = new Inner(3, "name_deleted", "desc_deleted", 30);
    }

    @Test
    void testBuildForList_Add() {
        ItemView<Inner> addView = new ItemView<>("key_add", null, addedItem);
        Map<Object, Inner> oldMap = new HashMap<>();
        Map<Object, Inner> newMap = Collections.singletonMap(addedItem.getId(), addedItem);

        DiffMapBuilder.buildForList(Collections.singletonList(addView), oldMap, newMap, Inner::getId, diffMap);

        // Assert that the ItemView's changeType is now set
        assertThat(addView.getChangeType()).isEqualTo("ADD");

        DiffMeta meta = diffMap.get("key_add");
        assertThat(meta).isNotNull();
        assertThat(meta.getChangeType()).isEqualTo("ADD");
        assertThat(meta.getOriginValues()).isNull();
        // This was the fix: fields should be populated from the new value
        assertThat(meta.getFields()).containsExactlyInAnyOrder("name", "desc");
    }

    @Test
    void testBuildForList_Delete() {
        ItemView<Inner> deleteView = new ItemView<>("key_delete", null, deletedItem);
        Map<Object, Inner> oldMap = Collections.singletonMap(deletedItem.getId(), deletedItem);
        Map<Object, Inner> newMap = new HashMap<>();

        DiffMapBuilder.buildForList(Collections.singletonList(deleteView), oldMap, newMap, Inner::getId, diffMap);

        // Assert that the ItemView's changeType is now set
        assertThat(deleteView.getChangeType()).isEqualTo("DELETE");

        DiffMeta meta = diffMap.get("key_delete");
        assertThat(meta).isNotNull();
        assertThat(meta.getChangeType()).isEqualTo("DELETE");
        assertThat(meta.getOriginValues()).isEqualTo(deletedItem);
        assertThat(meta.getFields()).containsExactlyInAnyOrder("name", "desc");
    }

    @Test
    void testBuildForList_Modify() {
        ItemView<Inner> modifyView = new ItemView<>("key_modify", null, newItem);
        Map<Object, Inner> oldMap = Collections.singletonMap(oldItem.getId(), oldItem);
        Map<Object, Inner> newMap = Collections.singletonMap(newItem.getId(), newItem);

        DiffMapBuilder.buildForList(Collections.singletonList(modifyView), oldMap, newMap, Inner::getId, diffMap);

        // Assert that the ItemView's changeType is now set
        assertThat(modifyView.getChangeType()).isEqualTo("MODIFY");

        DiffMeta meta = diffMap.get("key_modify");
        assertThat(meta).isNotNull();
        assertThat(meta.getChangeType()).isEqualTo("MODIFY");
        assertThat(meta.getOriginValues()).isEqualTo(oldItem);
        assertThat(meta.getFields()).containsExactly("name");
    }
    
    @Test
    void testBuildForList_Equal() {
        // Use oldItem as both old and new to simulate no change
        ItemView<Inner> equalView = new ItemView<>("key_equal", null, oldItem);
        Map<Object, Inner> oldMap = Collections.singletonMap(oldItem.getId(), oldItem);
        Map<Object, Inner> newMap = Collections.singletonMap(oldItem.getId(), oldItem);

        DiffMapBuilder.buildForList(Collections.singletonList(equalView), oldMap, newMap, Inner::getId, diffMap);

        // Assert that the ItemView's changeType is now set
        assertThat(equalView.getChangeType()).isEqualTo("EQUAL");

        DiffMeta meta = diffMap.get("key_equal");
        assertThat(meta).isNotNull();
        assertThat(meta.getChangeType()).isEqualTo("EQUAL");
        assertThat(meta.getOriginValues()).isEqualTo(oldItem);
        assertThat(meta.getFields()).isNull();
    }
}
