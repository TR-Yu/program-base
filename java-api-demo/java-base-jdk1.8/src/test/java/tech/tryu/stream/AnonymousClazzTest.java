package tech.tryu.stream;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tryu.App;
import tech.tryu.stream.anonymous.Bot;
import tech.tryu.stream.anonymous.ChatBot;

import java.util.concurrent.Callable;

@RunWith(BlockJUnit4ClassRunner.class)
public class AnonymousClazzTest {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * 匿名类的使用测试, 创建一个匿名类的对象，需要使用new关键字，后面跟着匿名类的实现接口或者继承类，
     * 后面跟着一对大括号，大括号中是匿名类的实现方法
     *
     */
    @Test
    public void createAnonymousTest() {
        final String testStr = "test";

        // 一个通过 implements 实现的匿名类, 匿名内部类 implements 了 Callable 接口，并重写了 call() 方法
        @SuppressWarnings("Convert2Lambda")
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() {
                return testStr;
            }
        };
        try {
            Assert.assertEquals(callable.call(), testStr);
        } catch (Exception e) {
            logger.error("error", e);
        }

        // 一个通过 extends 实现的匿名类, 匿名内部类 extends 了 ChatBot 类，并重写了 内部的两个方法
        ChatBot chatBot = new ChatBot(){
            @Override
            public String botReturn() {
                return testStr;
            }

        };
        Assert.assertEquals(chatBot.botReturn(), testStr);
        Assert.assertEquals(chatBot.isBot(), true);

        // 当实现的接口或者继承的类只有一个方法时，可以使用 lambda 表达式，否则不能使用 lambda 表达式
        Bot bot = new Bot() {
            @Override
            public String botReturn() {
                return testStr;
            }

            @Override
            public Boolean isBot() {
                return false;
            }
        };
        Assert.assertEquals(bot.botReturn(), testStr);
        Assert.assertEquals(bot.isBot(), false);

    }
}