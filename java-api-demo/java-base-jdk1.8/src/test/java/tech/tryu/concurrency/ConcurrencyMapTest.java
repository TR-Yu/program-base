package tech.tryu.concurrency;

import org.junit.Test;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class ConcurrencyMapTest {

    public final static Logger logger = org.slf4j.LoggerFactory.getLogger(ConcurrencyMapTest.class);

    @Test
    public void TreeMapTest() {

        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("a", 1);
        treeMap.put("aof", 2);
        treeMap.put("ltr", 3);
        treeMap.put("zoo",4);
        treeMap.put("foo",5);
        treeMap.forEach((k, v) -> logger.info("key: {}, value: {}", k, v));

        TreeMap<MyKey, Double> doubleTreeMap = new TreeMap<>((o1, o2) -> Integer.compare(o2.value, o1.value));
        Random random = new Random();
        doubleTreeMap.put(new MyKey("a", ThreadLocalRandom.current().nextInt()), random.nextDouble());
        doubleTreeMap.put(new MyKey("aof", ThreadLocalRandom.current().nextInt()), random.nextDouble());
        doubleTreeMap.put(new MyKey("ltr", ThreadLocalRandom.current().nextInt()), random.nextDouble());
        doubleTreeMap.put(new MyKey("zoo", ThreadLocalRandom.current().nextInt()), random.nextDouble());
        doubleTreeMap.forEach((k, v) -> logger.info("key: {},{}; value: {}", k.key,k.value, v));

    }

    static class MyKey {
        private String key;
        private int value;
        public MyKey(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

}