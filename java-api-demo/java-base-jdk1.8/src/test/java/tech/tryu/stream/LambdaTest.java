package tech.tryu.stream;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class LambdaTest {
    @Test
    public void BinaryOperatorTest() {
        List<Long> longs = new ArrayList() {{
            add(1L);
            add(2L);
            add(13L);
            add(4L);
            add(5L);
            add(6L);
            add(17L);
            add(8L);
            add(9L);
        }};
        String[] array = {"first", "second", "three", "twenty", "tree", "how", "why", "you", "the"};

        Optional<String> minString = Arrays.stream(array).min(String::compareTo);
        Optional<Long> min = longs.stream().min(Long::compareTo);

        Assert.assertEquals(minString, Optional.of("first"));
        Assert.assertEquals(min, Optional.of(1L));
    }
}