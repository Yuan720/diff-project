package com.github;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OuterView {
    private String value1;
    private String value2;
    private String value3;

    private String diffKey;

    private List<ItemView<Inner>> items;

    private Map<String, DiffMeta> diffMap;

    private Outer oldValue;
}
