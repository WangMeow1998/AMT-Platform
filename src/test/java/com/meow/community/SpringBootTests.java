package com.meow.community;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    //在测试类加载前调用，只调用一次

    public void testBeforeTestClass(){
        System.out.println("BeforeTestClass");
    }

    //在测试类销毁后调用，只调用一次

    public void testAfterTestClass(){
        System.out.println("AfterTestClass");
    }

    //在测试方法调用前，执行
    @BeforeEach
    public void testBefore(){
        System.out.println("Before");
    }

    //则测试方法调用后，执行
    @AfterEach
    public void testAfter(){
        System.out.println("After");
    }

    @Test
    public void test1(){
        System.out.println("test1");
    }

    @Test
    public void test2(){
        System.out.println("test2");
    }
}
