package com.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // 创建旧的 Outer 对象
        Outer oldObj = new Outer();
        oldObj.setValue1("v1");
        oldObj.setValue2("v2");
        oldObj.setValue3("v3");

        Inner inner1 = new Inner();
        inner1.setId(1);
        inner1.setName("n1");
        inner1.setDesc("d1");
        inner1.setAge(10);

        Inner inner2 = new Inner();
        inner2.setId(2);
        inner2.setName("n2");
        inner2.setDesc("d2");
        inner2.setAge(20);

        oldObj.setItems(Arrays.asList(inner1, inner2));

        // 创建新的 Outer 对象
        Outer newObj = new Outer();
        newObj.setValue1("v1");
        newObj.setValue2("v2-updated");  // value2 改了
        newObj.setValue3("v3-updated");  // value3 改了

        Inner inner3 = new Inner();
        inner3.setId(2);  // 这个内嵌对象是更新后的
        inner3.setName(null);  // 这个被删除
        inner3.setDesc("d2");
        inner3.setAge(20);

        Inner inner4 = new Inner();  // 新增的 inner 对象
        inner4.setId(3);
        inner4.setName("n3");
        inner4.setDesc("d3");
        inner4.setAge(30);

        newObj.setItems(Arrays.asList(inner3, inner4));  // 新增和更新

        System.out.println("-----old---");
        System.out.println(mapper.writeValueAsString(oldObj));
        System.out.println("-----new---");
        System.out.println(mapper.writeValueAsString(newObj));

        // 调用 diffEngine 来构建视图
        OuterView result = OuterDiffEngine.build(oldObj, newObj);
        System.out.println("-----result---");
        System.out.println(mapper.writeValueAsString(result));

/*        // 打印最终视图
        System.out.println("Final diff view:");
        System.out.println("Value1: " + result.getValue1());
        System.out.println("Value2: " + result.getValue2());
        System.out.println("Value3: " + result.getValue3());
        System.out.println("DiffKey: " + result.getDiffKey());

        // 打印 items
        for (ItemView<Inner> item : result.getItems()) {
            System.out.println("Item DiffKey: " + item.getDiffKey());
            System.out.println("Item DisplayState: " + item.getDisplayState());
            System.out.println("Item Data: " + item.getData());
        }

        // 打印 diffMap
        System.out.println("DiffMap: " + result.getDiffMap());*/
    }
}
