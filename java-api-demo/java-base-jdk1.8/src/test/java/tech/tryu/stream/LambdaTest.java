package tech.tryu.stream;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tryu.stream.entity.Hierarchical;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class LambdaTest {
    final static Logger logger = LoggerFactory.getLogger(LambdaTest.class);

    //region Optional
    @Test
    public void OptionalTest() {
        // 返回的数据包裹为 Optional
        result().ifPresent(
                list -> list.forEach(System.out::println)
        );

        // Optional 多层嵌套数据处理， 模拟处理和每层返回过滤
        Hierarchical hierarchical = new Hierarchical() {{
            setName("hierarchical");
            setFirst(new Hierarchical.First() {{
                setName("first");
                setSecond(new Hierarchical.Second() {{
                    setName("second");
                    setThird(new Hierarchical.Third() {{
                        setName("third");
                    }});
                }});
            }});
        }};
        testOptional(hierarchical);

        // Optional 的 map 和 flatMap 的区别，
        // map 会将 Optional 包裹的数据进行处理，直接返回的结果为 Optional 类型
        // 而 flatMap 返回结果需要包裹为 Optional 类型
        String str = "hello";
        Optional<Integer> integer = Optional.of(str).flatMap(s -> Optional.of(s.length()));
        Optional<Integer> integer02 = Optional.of(str).map(s -> s.length());


    }

    // 返回具体的处理结果，用 Optional 包一层，可避免空指针
    private Optional<List<String>> result() {
        return Optional.of(new ArrayList<>());
    }

    // 从多层嵌套数据中依次取出后处理
    private void testOptional(Hierarchical hierarchical) {
        final List<String> list = new ArrayList<>();
        Optional.ofNullable(hierarchical)
                .map(hierarchical1 -> {
                    Optional.ofNullable(hierarchical1.getName())
                            .filter(s -> s.equals("hierarchical"))
                            .ifPresent(list::add);
                    return hierarchical1.getFirst();
                })
                .map(first1 -> {
                    Optional.ofNullable(first1.getName())
                            .filter(s -> s.equals("first"))
                            .ifPresent(list::add);
                    return first1.getSecond();
                })
                .map(second -> {
                    Optional.ofNullable(second.getName())
                            .filter(s -> s.equals("second"))
                            .ifPresent(list::add);
                    return second.getThird();
                })
                .flatMap(third -> Optional.ofNullable(third.getName()).filter(s -> s.equals("third")))
                .ifPresent(list::add);
        logger.info("list: {}", list);
        logger.info("hierarchical: {}", hierarchical);
        Assert.assertEquals(list, Arrays.asList("hierarchical", "first", "second", "third"));
        Assert.assertEquals(hierarchical.toString(),
                "Hierarchical{name:'hierarchical', first:First{second :Second{third :Third{name :'third'}, name :'second'}, name :'first'}}");

    }

    //endregion

    //region Create Stream

    @Test
    public void CreateStreamTest() {
        /*
         Stream.of() 从数组或者集合中创建
         Stream.of(), Stream.empty(),
         Stream.generate(), Stream.iterate() 适合构造数据，构造出一个符合一定规则的 Entity Stream
         特别的 Stream 操作，
         limit : extracting a subset of a stream; skip concat : combining two streams
         */
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
        }};
        String[] array = {"first", "second", "three", "twenty", "tree", "how", "why", "you", "the"};
        Stream<Integer> listTransformStream = list.stream();
        Stream<String> arrayTransformStream = Arrays.stream(array);
        Stream<Integer> staticStream = Stream.of(1, 2, 3, 4, 5);
        Stream<Object> empty = Stream.empty();
        // Stream.generate() 生成无限流
        Stream<Double> doubleStream = Stream.generate(Math::random).limit(10);
        Stream<BigInteger> iterateStream = Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.TEN)).limit(10);
        // 验证：
        Assert.assertEquals(listTransformStream
                        .peek(el -> logger.info("listTransformStream: {}", el))
                        .count()
                , list.size());
        System.out.println("=====================================");
        Assert.assertEquals(arrayTransformStream
                        .peek(el -> logger.info("arrayTransformStream: {}", el))
                        .count()
                , array.length);
        System.out.println("=====================================");
        Assert.assertEquals(staticStream
                        .peek(el -> logger.info("staticStream: {}", el))
                        .count()
                , 5);
        System.out.println("=====================================");
        Assert.assertEquals(empty
                        .peek(el -> logger.info("empty: {}", el))
                        .count()
                , 0);
        System.out.println("=====================================");
        Assert.assertEquals(doubleStream.peek(el -> logger.info("doubleStream: {}", el)).count(), 10);
        System.out.println("=====================================");
        Assert.assertEquals(iterateStream.peek(el -> logger.info("iterateStream: {}", el)).count(), 10);

        System.out.println("=====================================");
        // Stream.concat() 合并两个 Stream, 不同元素类型的 Stream 也可以合并，但是合并后的 Stream 元素类型为 Object
        Stream.concat(list.stream(), Arrays.stream(array))
                .peek(el -> Assert.assertTrue(el instanceof Serializable))
                .forEach(el -> logger.info("difStream: {}", el));
        Stream.concat(list.stream(), list.stream())
                .peek(el -> Assert.assertTrue(el instanceof Integer))
                .forEach(el -> logger.info("concatStream: {}", el));

        Assert.assertFalse(null instanceof Integer);

        streamOrNull(null);

    }

    /**
     * Stream.of() 传入 null 仍然安全
     *
     * @param list
     */
    private void streamOrNull(List<String> list) {
        Stream.of(list).filter(Objects::nonNull).forEach(System.out::println);
    }
    //endregion

    //region Stream of Aggregate Operation

    /**
     * 有三类： 中间操作，终止操作，短路操作
     * 中间操作，返回 Stream 的操作，可以进行链式调用，返回的 Stream 可以继续调用中间操作;
     * filter, map, flatMap, distinct, sorted, peek, limit, skip, parallel, sequential, unordered
     * 终止操作,短路操作：返回非 Stream 的操作，只能调用一次，调用后 Stream 就会被关闭
     * forEach, forEachOrdered, toArray, reduce, collect, min, max, count,
     * anyMatch, allMatch, noneMatch, findFirst, findAny, iterator
     * <p>
     * map, flatmap 都是映射操作，区别在于
     * map 返回的是 Stream，而 flatMap 返回的是 Stream 的内容，可以将多个 Stream 合并为一个 Stream
     */
    @Test
    public void mapOperationTest() {
        String[] array = {"My", "name", "is", "Tom"};
        String[] intArray = {"123", "245689", "31234", "4890"};
        // map 映射操作，将 Stream 中的每个元素进行处理，返回一个新的 Stream
        Stream<String> mapStream = Arrays.stream(array).map(String::toUpperCase);
        // flatMap 映射操作，将 Stream<String> 中的每个元素 String 变成为 Stream<Character> 后进行合并，返回一个新的 Stream<Character>
        Stream<Integer> integerStream = Arrays.stream(intArray).flatMap(s -> {
            List<Integer> list = new ArrayList<>();
            for (char c : s.toCharArray()) {
                list.add(Integer.valueOf(String.valueOf(c)));
            }
            return list.stream();
        });


        mapStream.peek(el -> Assert.assertEquals(el, el.toUpperCase())).forEach(System.out::println);
        System.out.println("=====================================");
        long count = integerStream.peek(el -> {
            Assert.assertTrue(el >= 0 && el <= 9);
            System.out.println(el);
        }).count();
        Assert.assertEquals(count,
                intArray[0].length() + intArray[1].length() + intArray[2].length() + intArray[3].length());

    }

    /**
     * reduce(BinaryOperator<T> accumulator)
     * 表示将 Stream 中的元素两两按照操作规约。 v0,v1,v2,v3,v4,v5,v6,v7,v8,v9 操作就算 vi op vi+1
     * reduce(T identity, BinaryOperator<T> accumulator) identity 表示初始值，操作 v0 op vi op vi+1
     * 当 Stream 为空时，返回 identity
     * Stream()#reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner)
     * 第三个参数是为了并行操作时，将多个并行操作的结果进行合并，当为串行操作，则不会调用该参数，但是书写上还是需要写明，一般写为 (u, u2) -> u
     */
    @Test
    public void reduceOperationTest() {
        // 空值测试，当为空时，没有指定初始值，抛出异常
        ArrayList<Integer> emptyList = new ArrayList<>();
        Assert.assertThrows(NoSuchElementException.class, () -> emptyList.stream().reduce(Integer::sum).get());

        // 空值测试，当为空时，指定初始值，返回初始值
        Assert.assertEquals(emptyList.stream().reduce(0, Integer::sum), Integer.valueOf(0));

        ArrayList<Integer> intList = new ArrayList<Integer>() {{
            add(11);
            add(12);
            add(13);
            add(14);
            add(15);
        }};

        // 将 Integer 转变为字符串内的数据，然后拼接后输出
        String emptyStr = emptyList.stream().reduce("first:", (str, el) -> str + el + ",", (str1, str2) -> str1);
        String connectStr = intList.stream().reduce("first: ", (str, el) -> str + el + ",", (str1, str2) -> str1);
        Assert.assertEquals(emptyStr, "first:");
        Assert.assertEquals(connectStr, "first: 11,12,13,14,15,");

        // 假设有一个 List<String> ，用 reduces 操作将其转化为一个 HashMap<String> 注意在第三步归并中的时候取的是新生成的 HashMap
        List<String> list = Arrays.asList("a=1", "b=2", "c=3", "d=4");
        Map<String, String> mapReduce = list.stream().reduce(new HashMap<>(), (m, s) -> {
            String[] split = s.split("=");
            m.put(String.valueOf(split[0]), String.valueOf(split[1]));
            return m;
        }, (m1, m2) -> m1);

        list.forEach(el -> {
            String[] split = el.split("=");
            Assert.assertEquals(mapReduce.get(String.valueOf(split[0])), String.valueOf(split[1]));
        });
        mapReduce.forEach((key, value) -> logger.info("\nkey:{}, value: {}\n", key, value));

    }
    /**
     * 上一个 reduce 操作中的生成的 HashMap 在每一次过程中
     * 1. Stream.collect(Collector<? super T, A, R> collector)
     * 2. Stream.collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner)
     *  supplier: 创建一个新的结果容器 例如 ArrayList::new
     *  accumulator: 累加器将 Stream 中的元素添加到结果容器中 例如 ArrayList::add
     *  combiner: 将两个结果容器合并为一个 例如 ArrayList::addAll, 并行操作时会调用该方法，串行时不会调用
     *
     *  3. Stream.collect() 与 Stream.reduce() 的区别
     *  Stream.reduce() 是将 Stream 中的元素两两进行操作，最终得到一个结果
     *  Stream.collect() 是将 Stream 中的元素添加到结果容器中，最终得到一个结果容器
     */
    /**
     * Stream.collect() 补充：
     * Stream.collect(Collector<? super T, A, R> collector)
     * <p>
     * 1. Collector<T, A, R> 为一个接口，其中包含三个泛型参数,
     * 其实就是一个上面入参的
     * collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) 的三个参数的封装
     * T: Stream 中的元素类型 例如 Integer 或者 String 等
     * A: 中间结果容器类型 例如 List<Integer> 或者 Map<String, Integer> 等
     * R: 最终结果类型 例如 Integer 或者 String 等
     * 为了简化常用实现：
     * Collectors 提供了很多工厂方法，用于创建 Collector 接口的实现类
     * Collectors.toCollection() Collectors.toList()，Collectors.toSet()，Collectors.toMap()
     * Collectors.joining()，
     * Collectors.groupingBy()，Collectors.partitioningBy()
     * <p>
     * Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier)
     * keyMapper: 用于生成 Map 中的 key; valueMapper: 用于生成 Map 中的 value;
     * mergeFunction: 当 key 重复时，用于合并 value; mapSupplier: 用于创建 Map
     * 一般来说，mergeFunction 默认实现为 throwingMerger 即抛出异常； mapSupplier 默认实现为 HashMap::new
     * <p>
     * Collectors.joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix)
     * delimiter: 分隔符; prefix: 前缀; suffix: 后缀
     * delimiter 默认为 ""，prefix 默认为 ""，suffix 默认为 ""
     * <p>
     * collect(Collectors.groupingBy(classifier, downstream))
     * classifier 为一个 分组后的key 值；downstream 为一个 Collector, 用于收集 Stream 中的元素
     * 先将 Stream 按照指定规则获取key,然后相同 key 的为一组，再将每一组的元素收集到一个结果容器中做进一步处理
     * 进一步处理可以是将每一组的元素转化为一个 Collection 类型，例如 collections.toList(), collections.toSet()
     * 也可以是将每一组的元素做一些统计操作例如 Collections.mapping(), Collections.reducing()
     * Collectors.partitioningBy() 为特殊的 Collectors.groupingBy(); key 只有两种情况 true 和 false
     * <p>
     * collectors.reducing(U identity, Function<? super T, ? extends U> mapper, BinaryOperator<U> op)
     * identity: 初始值; mapper: 用于将 Stream 中的元素转化为 U 类型;
     * op: 用于将 集合内的 两两 U 类型的元素合并最终为一个 U 类型的元素
     */
    @Test
    public void collectOptionalTest() {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // collect( Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner)
        // 结果容器结果设置为 StringBuilder
        String string = list.stream()
                .collect(StringBuilder::new, StringBuilder::append, (sb1, sb2) -> {
                    sb1.append(sb2);
                    logger.info("printing {}", sb1.toString());
                }).toString();
        Assert.assertEquals(string, "12345678910");

        // Collectors.toMap(keyMapper, valueMapper, mergeFunction)
        // 用于将 Stream 转化为 Map
        Map<String, Integer> mapKey = list.stream().collect(Collectors.toMap(Object::toString, el -> el + 100, (v1, v2) -> v1));
        AtomicInteger count = new AtomicInteger();
        mapKey.forEach((key, value) -> {
            list.forEach(el -> {
                if (key.equals(el.toString())) {
                    Assert.assertEquals(value, Integer.valueOf(el + 100));
                    count.getAndIncrement();
                }
            });
        });
        Assert.assertEquals(count.get(), list.size());


        // primitive Type Stream 为了避免装箱拆箱，提供了 IntStream, LongStream, DoubleStream，
        // 他们有专用的方法，在 Collectors 中也有专用的方法

    }


    //endregion

    //region parallelStream

    /**
     *
     * parallelStream() 为 Stream 提供了并行操作的能力,默认采用的是 ForkJoinPool.commonPool() 线程池
     * parallel core size = Runtime.getRuntime().availableProcessors() -1
     * 在并行操作，要求本身就是无状态的，即不依赖于上一次操作的结果，而且共享变量是线程安全的
     * 在采用 ForkJoin Framework 的并行操作中, 不应该有阻塞操作，否则会导致线程阻塞
     * 一般不采用的原因是，ForkJoin Framework 采用的是 work-stealing 算法，即线程会从其他线程的任务队列中获取任务
     * 如果有阻塞操作，会导致其他线程无法获取任务，从而导致线程池中的线程全部阻塞，无法把控
     *
     */
    @Test
    public void parallelStreamTest() {
        List<String> strList = new ArrayList<String>() {{
            add("a");
            add("b");
            add("c");
            add("d");
            add("e");
            add("f");}
        };
        strList.parallelStream().forEach(str -> {
            logger.info("parallelStreamTest: {}", str);
        });
    }
    //endregion


    @Test
    public void PredicateTest(){
        String[] strings = {"a", "b", "c", "d", "e", "f"};
        Arrays.stream(strings)
                 .filter(((Predicate<String>) s -> Objects.equals(s, "a")).or(s -> Objects.equals(s, "b")))
                 .forEach(s -> Assert.assertTrue(Objects.equals(s, "a") || Objects.equals(s, "b")));

        Arrays.stream(strings)
                .filter(Predicate.isEqual("a"))
                .forEach(s -> Assert.assertEquals("a", s));

        Optional<Object> empty = Optional.empty();
        Optional<Object> nullOptional = Optional.ofNullable(null);
        Optional<String> s1 = empty.map(s -> s.toString());
        Optional<String> s2 = nullOptional.map(s -> s.toString());
        Assert.assertThrows(NoSuchElementException.class, empty::get);
        Assert.assertThrows(NoSuchElementException.class, nullOptional::get);


    }

    @Test
    public void CountTest(){
        List<Integer> list0 = new ArrayList<Integer>();
        long start0 = System.currentTimeMillis();
        // 4 Bytes * 10_000_000 = 40 MB
        for (int i = 0; i < 10000000; i++) {
            list0.add(i);
        }
        System.out.println(System.currentTimeMillis() - start0);

        long start1 = System.currentTimeMillis();
        List<Integer> list1 = new ArrayList<Integer>();
        for (int i = 10000000; i < 20000000; i++) {
            list1.add(i);
        }
        System.out.println(System.currentTimeMillis() - start1);

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));

        long start2 = System.currentTimeMillis();
        List<Integer> list2 = new ArrayList<Integer>();
        for (int i = 20000000; i < 30000000; i++) {
            list2.add(i);
        }
        System.out.println(System.currentTimeMillis() - start2);
    }



}
