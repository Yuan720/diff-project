package com.github;


import lombok.Data;

@Data
public class Inner {
    @Uniq
    int id;
    @CompareField
    String name;
    @CompareField
    String desc;
    int age;
}
