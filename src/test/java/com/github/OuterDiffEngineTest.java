package com.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OuterDiffEngineTest {

    private Outer oldOuter;
    private Outer newOuter;

    @BeforeEach
    void setUp() {
        // It's crucial to reset the key generator before each test
        // to have predictable diff keys ("key1", "key2", ...).
        DiffKeyGenerator.reset();

        // Old state
        oldOuter = new Outer(
                "val1_old", "val2_old", "val3_old",
                Arrays.asList(
                        new Inner(1, "item1_name_old", "item1_desc", 20), // Will be modified
                        new Inner(2, "item2_name", "item2_desc", 30),     // Will be deleted
                        new Inner(3, "item3_name", "item3_desc", 40)      // Will be equal
                )
        );

        // New state
        newOuter = new Outer(
                "val1_new", "val2_old", "val3_new", // value1 and value3 modified
                Arrays.asList(
                        new Inner(1, "item1_name_new", "item1_desc", 20), // Modified
                        new Inner(3, "item3_name", "item3_desc", 40),     // Equal
                        new Inner(4, "item4_name", "item4_desc", 50)      // Added
                )
        );
    }

    @Test
    void testBuild() {
        OuterView view = OuterDiffEngine.build(oldOuter, newOuter);

        // 1. Test top-level OuterView fields
        assertThat(view.getValue1()).isEqualTo("val1_new");
        assertThat(view.getValue2()).isEqualTo("val2_old");
        assertThat(view.getValue3()).isEqualTo("val3_new");
        assertThat(view.getOldValue()).isEqualTo(oldOuter); // Verify oldValue is set
        assertThat(view.getDiffKey()).isEqualTo("key1");   // Root diff key
        assertThat(view.getDiffMap()).isNotNull();
        // The merged list contains old items (deleted/modified/equal) and new items (added)
        assertThat(view.getItems()).hasSize(4);

        // 2. Test root DiffMeta (verifying data redundancy fix)
        DiffMeta rootMeta = view.getDiffMap().get("key1");
        assertThat(rootMeta).isNotNull();
        assertThat(rootMeta.getChangeType()).isEqualTo("MODIFY");
        assertThat(rootMeta.getFields()).containsExactlyInAnyOrder("value1", "value3");

        Object originValues = rootMeta.getOriginValues();
        assertThat(originValues).isInstanceOf(Outer.class);
        Outer originOuter = (Outer) originValues;
        assertThat(originOuter.getValue1()).isEqualTo("val1_old");
        assertThat(originOuter.getItems()).isNullOrEmpty(); // VERIFIES THE REDUNDANCY FIX

        // 3. Test DiffMeta and ItemView states for each item change type
        List<ItemView<Inner>> items = view.getItems();

        // Test for MODIFIED item (id=1)
        ItemView<Inner> modifiedView = findItemById(items, 1);

        assertThat(modifiedView.getChangeType()).isEqualTo("MODIFY"); // New assertion
        DiffMeta modifiedMeta = view.getDiffMap().get(modifiedView.getDiffKey());
        assertThat(modifiedMeta.getChangeType()).isEqualTo("MODIFY");
        assertThat(modifiedMeta.getFields()).containsExactly("name");
        assertThat(modifiedMeta.getOriginValues()).isEqualTo(oldOuter.getItems().get(0));

        // Test for DELETED item (id=2)
        ItemView<Inner> deletedView = findItemById(items, 2);

        assertThat(deletedView.getChangeType()).isEqualTo("DELETE"); // New assertion
        DiffMeta deletedMeta = view.getDiffMap().get(deletedView.getDiffKey());
        assertThat(deletedMeta.getChangeType()).isEqualTo("DELETE");
        assertThat(deletedMeta.getFields()).containsExactlyInAnyOrder("name", "desc");
        assertThat(deletedMeta.getOriginValues()).isEqualTo(oldOuter.getItems().get(1));

        // Test for EQUAL item (id=3)
        ItemView<Inner> equalView = findItemById(items, 3);

        assertThat(equalView.getChangeType()).isEqualTo("EQUAL"); // New assertion
        DiffMeta equalMeta = view.getDiffMap().get(equalView.getDiffKey());
        assertThat(equalMeta.getChangeType()).isEqualTo("EQUAL");
        assertThat(equalMeta.getFields()).isNull();
        assertThat(equalMeta.getOriginValues()).isEqualTo(oldOuter.getItems().get(2));

        // Test for ADDED item (id=4)
        ItemView<Inner> addedView = findItemById(items, 4);

        assertThat(addedView.getChangeType()).isEqualTo("ADD"); // New assertion
        DiffMeta addedMeta = view.getDiffMap().get(addedView.getDiffKey());
        assertThat(addedMeta.getChangeType()).isEqualTo("ADD");
        // Verifies the "record new fields on ADD" fix
        assertThat(addedMeta.getFields()).containsExactlyInAnyOrder("name", "desc");
        assertThat(addedMeta.getOriginValues()).isNull();
    }

    private ItemView<Inner> findItemById(List<ItemView<Inner>> items, int id) {
        Optional<ItemView<Inner>> found = items.stream()
                .filter(item -> item.getData().getId() == id)
                .findFirst();
        assertThat(found).as("Item with id %d not found in merged list", id).isPresent();
        return found.get();
    }
}

