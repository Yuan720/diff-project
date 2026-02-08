# Java 对象深度比对工具

## 项目简介

本项目是一个 Java 工具库，用于深度比对两个对象（`Outer` 类型），并生成一个详细的差异报告。它能够识别顶层字段的变更，以及内嵌列表（`items`）中的项目增、删、改情况。

## 核心概念

*   **DiffKey**: 为每一个参与比对的元素（包括根对象和列表中的每一项）生成的唯一标识符。格式为 `keyN`，例如 `key1`, `key2`。
*   **DiffMap**: 一个 `Map<String, DiffMeta>` 结构，存储了整个比对过程中的所有变更详情。`key` 是元素的 `DiffKey`，`value` 是描述该元素变更信息的 `DiffMeta` 对象。
*   **ChangeType**: 描述变更类型的字符串，有以下几种：
    *   `ADD`: 新增
    *   `DELETE`: 删除
    *   `MODIFY`: 修改
    *   `EQUAL`: 无变化
*   **DisplayState**: 列表项视图（`ItemView`）的状态，用于在前端或UI上展示。
    *   `NORMAL`: 正常状态，表示该项在新的列表中存在。
    *   `DELETED`: 删除状态，表示该项在旧的列表中存在，但在新列表中已被删除。

## 主要类和作用

### 注解 (Annotations)

*   **`@CompareField`**: 标记在字段上，表示该字段需要进行比对。只有标记了此注解的字段才会被纳入 `MODIFY` 类型的变更检测。
*   **`@Uniq`**: 标记在列表项（如 `Inner` 类）的某个字段上，表示该字段是列表项的唯一标识符（例如 `id`）。工具通过此字段来区分不同的列表项。

### 数据模型 (Data Models)

*   **`Outer.java`**: 主比对对象，包含一些基本类型字段和 `Inner` 对象的列表 `items`。
*   **`Inner.java`**: 内嵌对象，代表 `Outer` 对象中列表的一项。
*   **`DiffMeta.java`**: 差异元数据。用于存储一个元素的具体变更信息。
    *   `changeType`: 变更类型 (`ADD`, `DELETE`, `MODIFY`, `EQUAL`)。
    *   `fields`: 对于 `MODIFY` 类型，记录发生变更的字段名列表。对于 `ADD` 或 `DELETE` 类型，记录所有被 `@CompareField` 标记的字段。
    *   `originValues`: 记录变更前的原始对象值。对于 `ADD` 类型，此值为 `null`。
*   **`ItemView.java`**: 列表项的视图模型。它包装了原始的数据对象（如 `Inner`），并附加了 `diffKey` 和 `displayState`，便于前端渲染和追踪。
*   **`OuterView.java`**: 最终返回给调用者的视图模型。
    *   `value1`, `value2`, `value3`: `Outer` 对象的新值。
    *   `oldValue`: 原始的 `Outer` 对象。
    *   `items`: 合并后的 `ItemView<Inner>` 列表，包含了所有旧项和新项。
    *   `diffMap`: 完整的差异信息图。
    *   `diffKey`: 根对象 `Outer` 的 `diffKey`。

### 核心逻辑 (Core Logic)

*   **`DiffKeyGenerator.java`**:
    *   `next()`: 生成一个全局唯一的 `diffKey`。
    *   `reset()`: 重置生成器，使 `diffKey` 从 `key1` 重新开始。在每次独立的比对操作前调用，确保 `key` 的可预测性。
*   **`ListMergeUtil.java`**:
    *   `merge(oldList, newList, ...)`: 核心列表比对逻辑。它接收新旧两个列表，返回一个合并后的 `ItemView` 列表。这个返回的列表会包含所有旧列表的项（标记为 `DELETED` 或 `NORMAL`）和所有新列表但不在旧列表中的项（标记为 `NORMAL`）。
*   **`DiffMapBuilder.java`**:
    *   `buildForList(...)`: 遍历 `ListMergeUtil` 生成的 `ItemView` 列表，为每一个列表项创建 `DiffMeta` 对象，并将其存入 `diffMap`。
    *   `diffFields(oldObj, newObj)`: 比对两个对象中标有 `@CompareField` 的字段，返回一个包含所有值不相同的字段名的列表。
*   **`OuterDiffEngine.java`**:
    *   `build(oldObj, newObj)`: 整个比对过程的入口和编排者。它接收一个旧对象和一个新对象，执行以下操作：
        1.  比对 `Outer` 对象的顶层字段。
        2.  调用 `ListMergeUtil` 比对 `items` 列表。
        3.  调用 `DiffMapBuilder` 构建 `diffMap`。
        4.  组装并返回最终的 `OuterView`。

## 如何使用

1.  创建 `oldOuter` 和 `newOuter` 两个 `Outer` 对象实例。
2.  调用 `OuterView view = OuterDiffEngine.build(oldOuter, newOuter);`。
3.  `view` 对象即包含了完整的比对结果。你可以检查 `view.getDiffMap()` 来获取每个元素的详细变更，并使用 `view.getItems()` 来渲染列表。
