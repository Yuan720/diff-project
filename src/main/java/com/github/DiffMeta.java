package com.github;

import lombok.Data;

import java.util.List;

@Data
public class DiffMeta {
    private String changeType; // ADD | DELETE | MODIFY | EQUAL
    private List<String> fields;
    private Object originValues;
}
