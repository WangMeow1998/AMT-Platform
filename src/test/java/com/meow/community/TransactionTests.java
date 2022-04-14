package com.meow.community;


import com.meow.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTests {

    @Autowired
    private AlphaService alphaService;
    @Test
    public void testSave1(){
        alphaService.save1();
    }
    @Test
    public void testSave2(){
        alphaService.save2();
    }
}
