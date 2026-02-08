package com.github;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiffKeyGeneratorTest {

    @Test
    void testNextAndReset() {
        // Ensure reset() works from a clean state
        DiffKeyGenerator.reset();
        assertThat(DiffKeyGenerator.next()).isEqualTo("key1");
        assertThat(DiffKeyGenerator.next()).isEqualTo("key2");
        assertThat(DiffKeyGenerator.next()).isEqualTo("key3");

        // Reset the generator
        DiffKeyGenerator.reset();

        // After reset, the key should start from 1 again
        assertThat(DiffKeyGenerator.next()).isEqualTo("key1");
        assertThat(DiffKeyGenerator.next()).isEqualTo("key2");

        // Another reset to be sure
        DiffKeyGenerator.reset();
        assertThat(DiffKeyGenerator.next()).isEqualTo("key1");
    }
}
