package com.github;


import lombok.Data;

import java.util.List;

@Data
public class Outer {
    @CompareField
    String value1;
    @CompareField
    String value2;
    @CompareField
    String value3;
    List<Inner> items;
}
