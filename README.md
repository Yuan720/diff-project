# Java 对象深度比对工具

## 项目简介

本项目是一个 Java 工具库，用于深度比对两个对象（`Outer` 类型），并生成一个详细的差异报告。它能够识别顶层字段的变更，以及内嵌列表（`items`）中的项目增、删、改情况。

## 核心概念

*   **DiffKey**: 为每一个参与比对的元素（包括根对象和列表中的每一项）生成的唯一标识符。格式为 `keyN`，例如 `key1`, `key2`。
*   **DiffMap**: 一个 `Map<String, DiffMeta>` 结构，存储了整个比对过程中的所有变更详情。`key` 是元素的 `DiffKey`，`value` 是描述该元素变更信息的 `DiffMeta` 对象。
*   **`ChangeType`**: 描述一个元素（根对象或列表项）变更状态的字符串，是**最核心的状态字段**。它有以下几种值：
    *   `ADD`: 表示该项为新增。
    *   `DELETE`: 表示该项已被删除。
    *   `MODIFY`: 表示该项的内容发生了修改。
    *   `EQUAL`: 表示该项内容完全没有变化。

## 主要类和作用

### 注解 (Annotations)

*   **`@CompareField`**: 标记在字段上，表示该字段需要进行比对。只有标记了此注解的字段才会被纳入 `MODIFY` 类型的变更检测。
*   **`@Uniq`**: 标记在列表项（如 `Inner` 类）的某个字段上，表示该字段是列表项的唯一标识符（例如 `id`）。工具通过此字段来区分不同的列表项。

### 数据模型 (Data Models)

*   **`Outer.java`**: 主比对对象，包含一些基本类型字段和 `Inner` 对象的列表 `items`。
*   **`Inner.java`**: 内嵌对象，代表 `Outer` 对象中列表的一项。
*   **`DiffMeta.java`**: **(高级)** 差异元数据。用于存储一个元素的**详细**变更信息，主要用于深入分析 `MODIFY` 状态。
    *   `changeType`: 变更类型，与 `ItemView` 中的 `changeType` 值同步。
    *   `fields`: 对于 `MODIFY` 类型，记录发生变更的字段名列表。
    *   `originValues`: 记录变更前的原始对象值。
*   **`ItemView.java`**: 列表项的视图模型。它包装了原始的数据对象（如 `Inner`），并附加了完整的状态信息，是UI渲染的**主要数据源**。
    *   `diffKey`: 元素的唯一标识符。
    *   `changeType`: **核心状态字段** (`ADD` | `MODIFY` | `EQUAL` | `DELETE`)。
    *   `data`: 原始数据对象。
*   **`OuterView.java`**: 最终返回给调用者的视图模型。
    *   `value1`, `value2`, `value3`: `Outer` 对象的新值。
    *   `oldValue`: 原始的 `Outer` 对象。
    *   `items`: 合并后的 `ItemView<Inner>` 列表，包含了所有旧项和新项，**且每项都包含了唯一的 `changeType` 状态**。
    *   `diffMap`: **(高级)** 完整的差异信息图，用于在 `MODIFY` 状态下获取具体变更的字段等详细信息。
    *   `diffKey`: 根对象 `Outer` 的 `diffKey`。

### 核心逻辑 (Core Logic)

*   **`DiffKeyGenerator.java`**: 
    *   `next()`: 生成一个全局唯一的 `diffKey`。
    *   `reset()`: 重置生成器。
*   **`ListMergeUtil.java`**: 
    *   `merge(oldList, newList, ...)`: 核心列表比对逻辑，负责创建包含所有新旧列表项的统一列表 `ItemView`。
*   **`DiffMapBuilder.java`**: 
    *   `buildForList(...)`: 遍历合并后的列表，为每一个列表项计算 `changeType`，创建 `DiffMeta` 对象，并**将 `changeType` 设置到 `ItemView` 上**。
    *   `diffFields(oldObj, newObj)`: 比对两个对象中标有 `@CompareField` 的字段。
*   **`OuterDiffEngine.java`**: 
    *   `build(oldObj, newObj)`: 整个比对过程的入口和编排者。

## 如何使用

1.  创建 `oldOuter` 和 `newOuter` 两个 `Outer` 对象实例。
2.  调用 `OuterView view = OuterDiffEngine.build(oldOuter, newOuter);`。
3.  `view` 对象即包含了完整的比对结果。您可以直接遍历 `view.getItems()` 并根据 `item.getChangeType()` 来渲染UI。

**示例：**
```java
// 伪代码
for (ItemView<Inner> item : view.getItems()) {
    switch (item.getChangeType()) {
        case "DELETE":
            // 渲染为删除样式 (例如灰色、删除线)
            renderAsDeleted(item.getData());
            break;
        case "ADD":
            // 渲染为新增样式 (例如绿色高亮)
            renderAsAdded(item.getData());
            break;
        case "MODIFY":
            // 渲染为修改样式 (例如黄色高亮)
            renderAsModified(item.getData());
            // 如果需要知道具体哪些字段变了，此时可以查询 diffMap (高级用法)
            // DiffMeta details = view.getDiffMap().get(item.getDiffKey());
            // List<String> changedFields = details.getFields();
            break;
        case "EQUAL":
            // 正常渲染，无高亮
            renderAsNormal(item.getData());
            break;
    }
}
```
