package com.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ListMergeUtilTest {

    private List<Inner> oldList;
    private List<Inner> newList;

    @BeforeEach
    void setUp() {
        DiffKeyGenerator.reset();
    }

    @Test
    void testMergeWithAddModifyDelete() {
        oldList = Arrays.asList(
                new Inner(1, "item1", "desc1", 10), // Equal
                new Inner(2, "item2", "desc2", 20), // Deleted
                new Inner(3, "item3", "desc3", 30)  // Modified (but merge doesn't care about content)
        );
        newList = Arrays.asList(
                new Inner(1, "item1", "desc1", 10), // Equal
                new Inner(3, "item3_mod", "desc3_mod", 33), // Modified
                new Inner(4, "item4", "desc4", 40)          // Added
        );

        List<ItemView<Inner>> merged = ListMergeUtil.merge(oldList, newList, i -> i.id, i -> "key");

        assertThat(merged).hasSize(4);
        
        // Check for item 1 (Equal)
        ItemView<Inner> itemView1 = findItemById(merged, 1);
        assertThat(itemView1.getData().getName()).isEqualTo("item1");

        // Check for item 2 (Deleted)
        ItemView<Inner> itemView2 = findItemById(merged, 2);
        assertThat(itemView2).isNotNull();
        
        // Check for item 3 (Modified)
        ItemView<Inner> itemView3 = findItemById(merged, 3);
        assertThat(itemView3.getData().getName()).isEqualTo("item3_mod");

        // Check for item 4 (Added)
        ItemView<Inner> itemView4 = findItemById(merged, 4);
        assertThat(itemView4.getData().getName()).isEqualTo("item4");

        // Check order
        List<Integer> ids = merged.stream().map(iv -> iv.getData().getId()).collect(Collectors.toList());
        assertThat(ids).containsExactly(1, 2, 3, 4);
    }
    
    @Test
    void testAllAdded() {
        oldList = Collections.emptyList();
        newList = Arrays.asList(
                new Inner(1, "item1", "desc1", 10),
                new Inner(2, "item2", "desc2", 20)
        );

        List<ItemView<Inner>> merged = ListMergeUtil.merge(oldList, newList, i -> i.id, i -> "key");
        assertThat(merged).hasSize(2);
        assertThat(merged.get(0).getData().getId()).isEqualTo(1);
        assertThat(merged.get(1).getData().getId()).isEqualTo(2);
    }

    @Test
    void testAllDeleted() {
        oldList = Arrays.asList(
                new Inner(1, "item1", "desc1", 10),
                new Inner(2, "item2", "desc2", 20)
        );
        newList = Collections.emptyList();
        
        List<ItemView<Inner>> merged = ListMergeUtil.merge(oldList, newList, i -> i.id, i -> "key");
        assertThat(merged).hasSize(2);
        assertThat(merged.get(0).getData().getId()).isEqualTo(1);
        assertThat(merged.get(1).getData().getId()).isEqualTo(2);
    }

    @Test
    void testReorderingAndInsertion() {
        // This tests the anchor logic more deeply
        oldList = Arrays.asList(
                new Inner(1, "item1", "", 0),
                new Inner(2, "item2", "", 0),
                new Inner(3, "item3", "", 0)
        );
        // New list: item3 is moved to the front, item5 is inserted between 1 and 2
        newList = Arrays.asList(
                new Inner(3, "item3", "", 0),
                new Inner(1, "item1", "", 0),
                new Inner(5, "item5", "", 0), // Added, anchor should be item 1
                new Inner(2, "item2", "", 0)
        );

        List<ItemView<Inner>> merged = ListMergeUtil.merge(oldList, newList, i -> i.id, i -> "key");
        
        // With "Historical Priority" logic, the old list provides the skeleton.
        // New items are inserted relative to their anchors. '5' is anchored to '1' (in new list).
        List<Integer> ids = merged.stream().map(iv -> iv.getData().getId()).collect(Collectors.toList());
        assertThat(ids).containsExactly(1, 5, 2, 3);
    }

    @Test
    void testUserReportedSortingBug() {
        // As reported by the user
        oldList = Arrays.asList(
                new Inner(5, "n5", "d5", 10),
                new Inner(0, null, null, 0),
                new Inner(2, "n2", "d2", 20)
        );
        newList = Arrays.asList(
                new Inner(3, "n3", "d3", 30),
                new Inner(2, "n2", "d2", 20)
        );

        List<ItemView<Inner>> merged = ListMergeUtil.merge(oldList, newList, i -> i.id, i -> "key");

        // Expected: With "Historical Priority", the old list is the skeleton [5, 0, 2].
        // The new item '3' is anchored before '2' from the new list.
        // So '3' is inserted before '2' in the result.
        List<Integer> ids = merged.stream().map(iv -> iv.getData().getId()).collect(Collectors.toList());
        assertThat(ids).containsExactly(5, 0, 3, 2);
    }
    
    private ItemView<Inner> findItemById(List<ItemView<Inner>> items, int id) {
        return items.stream()
                .filter(item -> item.getData().getId() == id)
                .findFirst()
                .orElse(null);
    }
}
