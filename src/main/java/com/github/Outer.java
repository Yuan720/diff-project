package com.github;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Outer {
    @CompareField
    String value1;
    @CompareField
    String value2;
    @CompareField
    String value3;
    List<Inner> items;
}
