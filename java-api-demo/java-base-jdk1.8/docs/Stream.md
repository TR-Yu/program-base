# Stream

函数式编程
面对对象编程对数据进行抽象，而函数式编程对**行为进行抽象**。使用**不可变值**和函数；函数对一个值进行计算，返回一个新值。
这种编程方式可以避免副作用，使得程序更加可预测，更加容易理解和维护。

匿名内部类的使用
AnonymousClazzTest#createAnonymousTest
表明了匿名内部类的使用场景，当一个类只需要**使用一次**且需要实现接口或是改写类的**接口方法**的时候，可以使用匿名内部类，而避免创建一个类文件。
注意：一个匿名内部类只能实现一个接口或继承一个类。
与 Lambda 表达式的关系，当一个接口只有一个方法的时候，匿名内部类和 Lambda 表达式是等价的。其它情况下，则不能简写为 Lambda 表达式。

`@FunctionalInterface` 的使用
`@FunctionalInterface` 是 Java 8 引入的注解，用于表示一个接口是函数式接口，只包含**一个抽象方法**。可以用作 Lambda 表达式的目标类型。
注意：`@FunctionalInterface` 注解在编译时会检查被注解的接口**是否只包含一个默认重写的抽象方法**，，如果接口包含多于一个抽象方法，或者没有抽象方法，则编译时会报错。

关于引用的外部的变量，在匿名函数还是 Lambda 表达式中，都是不能修改的，否则会报错。
一般情况下，这种情况下，可以使用 final 修饰符来修饰这个变量，Java 8 以上可以不用修饰，但是必须是即成事实地不可变的。
为了**语义上的清晰**，建议使用 final 修饰符。

如何处理 Lambda 表达式中的异常
比较好的实现为，将 Lambda 表达式中的异常抛出，然后在调用 Lambda 表达式的地方进行捕获处理。
优雅地处理是将 Lambda 表达式定义一个异常捕获并处理的 Wrapper Function，然后在调用 Lambda 表达式的地方进行调用。

Optional 类

Optional 类是 Java 8 引入的一个新的类，用于解决空指针异常的问题。
创建 Optional 对象的方法: `Optional.ofNullable()`, `Optional.of()`, `Optional.empty()`
Optional 类的方法: `isPresent()`, `ifPresent()`, `get()`, `orElse()`, `orElseGet()`, `orElseThrow()`, `map()`, `flatMap()`, `filter()`
Optional 的 map 和 flatMap 的区别，
map 会将 Optional 包裹的数据进行处理，直接返回的结果为 Optional 类型
而 flatMap 返回结果需要包裹为 Optional 类型
Optional 多层嵌套数据处理， 模拟处理和每层返回过滤后的 Optional 对象。返回最终的 Optional 对象。

Stream 类
Stream 创建：
从数组或者集合中创建
`Stream.of()`, `Stream.empty()`,
`Stream.generate()`, `Stream.iterate()` 适合构造数据，构造出一个符合一定规则的 Entity Stream
特别的 Stream 操作，
`limit()` : extracting a subset of a stream; `skip()`, `concat()` : combining two streams

Stream 操作有三类： 中间操作，终止操作，短路操作
中间操作，返回 Stream 的操作，可以进行链式调用，返回的 Stream 可以继续调用中间操作;
filter, map, flatMap, distinct, sorted, peek, limit, skip, parallel, sequential, unordered(parallelStream 时使用)
终止操作,短路操作：返回非 Stream 的操作，只能调用一次，调用后 Stream 就会被关闭
forEach, forEachOrdered, toArray, reduce, collect, min, max, count,
anyMatch, allMatch, noneMatch, findFirst, findAny, iterator

Stream 的 map 和 flatMap 的区别
map, flatMap 都是映射操作，区别在于 map 返回的是 Stream，而 flatMap 返回的是 Stream 的内容，可以将多个 Stream 合并为一个 Stream

Stream 的 reduce 操作
`reduce(BinaryOperator<T> accumulator)`c表示将 Stream 中的元素两两按照操作规约。 v0,v1,v2,v3,v4,v5,v6,v7,v8,v9 操作就算 vi op vi+1
`reduce(T identity, BinaryOperator<T> accumulator)` identity 表示初始值，操作 v0 op vi op vi+1
当 Stream 为空时，返回 identity
`Stream().reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner)`
第三个参数是为了并行操作时，将多个并行操作的结果进行合并，当为串行操作，则不会调用该参数，但是书写上还是需要写明，一般写为 (u, u2) -> u

Stream 的 collect 操作
`Stream.collect(Collector<? super T, A, R> collector)`
`Stream.collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner)`
supplier: 创建一个新的结果容器 例如 ArrayList::new
accumulator: 累加器将 Stream 中的元素添加到结果容器中 例如 ArrayList::add
combiner: 将两个结果容器合并为一个 例如 ArrayList::addAll, 并行操作时会调用该方法，串行时不会调用
`Stream.collect()` 与 Stream.reduce() 的区别
`Stream.reduce()` 是将 Stream 中的元素两两进行操作，最终得到一个结果
`Stream.collect()` 是将 Stream 中的元素添加到结果容器中，最终得到一个结果容器

`Collector<T, A, R>` 为一个接口，其中包含三个泛型参数,
其实就是一个上面入参的 `collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner)` 的三个参数的封装
T: Stream 中的元素类型 例如 Integer 或者 String 等
A: 中间结果容器类型 例如 `List<Integer>` 或者 `Map<String, Integer>` 等
R: 最终结果类型 例如 Integer 或者 String 等
为了简化常用实现：
Collectors 提供了很多工厂方法，用于创建 Collector 接口的实现类
`Collectors.toCollection()`,`Collectors.toList()`，`Collectors.toSet()`，`Collectors.toMap()`
`Collectors.joining()`
`Collectors.groupingBy()`，`Collectors.partitioningBy()`

`Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier)`
keyMapper: 用于生成 Map 中的 key; valueMapper: 用于生成 Map 中的 value;
mergeFunction: 当 key 重复时，用于合并 value; mapSupplier: 用于创建 Map
一般来说，mergeFunction 默认实现为 throwingMerger 即抛出异常； mapSupplier 默认实现为 HashMap::new
`Collectors.joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix)`
delimiter: 分隔符; prefix: 前缀; suffix: 后缀
delimiter 默认为 ""，prefix 默认为 ""，suffix 默认为 ""

`collect(Collectors.groupingBy(classifier, downstream))`
classifier 为一个 分组后的key 值；downstream 为一个 Collector, 用于收集 Stream 中的元素
先将 Stream 按照指定规则获取key,然后相同 key 的分为一组，再将每一组的元素收集到一个结果容器中做进一步处理
进一步处理可以是将每一组的元素转化为一个 Collection 类型，例如 collections.toList(), collections.toSet()
也可以是将每一组的元素做一些统计操作例如 Collections.mapping(), Collections.reducing()
Collectors.partitioningBy() 为特殊的 Collectors.groupingBy(); key 只有两种情况 true 和 false

`collectors.reducing(U identity, Function<? super T, ? extends U> mapper, BinaryOperator<U> op)`
identity: 初始值; mapper: 用于将 Stream 中的元素转化为 U 类型;
op: 用于将 集合内的 两两 U 类型的元素合并最终为一个 U 类型的元素
