package com.github;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemView<T> {
    private String diffKey;
    private String changeType;   // ADD | DELETE | MODIFY | EQUAL
    private T data;
}
