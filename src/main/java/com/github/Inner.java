package com.github;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inner {
    @Uniq
    int id;
    @CompareField
    String name;
    @CompareField
    String desc;
    int age;
}
