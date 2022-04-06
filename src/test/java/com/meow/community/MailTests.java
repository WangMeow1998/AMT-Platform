package com.meow.community;


import com.meow.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;
    @Test
    public void testMail(){
        mailClient.sendMail("wangmeow1998@163.com","Test","Welcome!");
    }
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","wangmeow");
        String content = templateEngine.process("mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("wangmeow1998@163.com","Test-2",content);
    }
}
