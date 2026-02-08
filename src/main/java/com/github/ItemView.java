package com.github;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemView<T> {
    private String diffKey;
    private String displayState; // NORMAL | DELETED
    private T data;
}
