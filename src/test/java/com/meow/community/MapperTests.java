package com.meow.community;

import com.meow.community.dao.DiscussPostMapper;
import com.meow.community.dao.LoginTicketMapper;
import com.meow.community.dao.MessageMapper;
import com.meow.community.dao.UserMapper;
import com.meow.community.entity.DiscussPost;
import com.meow.community.entity.LoginTicket;
import com.meow.community.entity.Message;
import com.meow.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }
    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("wangmeow");
        user.setPassword("123456");
        user.setSalt("abcd");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
    }
    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(150,1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150,"http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150,"233");
        System.out.println(rows);
    }
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectDiscussPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10, 0);
        for(DiscussPost discussPost : list){
            System.out.println(discussPost);
        }
    }
    @Test
    public void testSelectDiscussPostRows(){
        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(102);
        loginTicket.setTicket("abcd");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        int rows = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(rows);
    }
    @Test
    public void testSelectAndUpdateLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abcd");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abcd",1);
        loginTicket = loginTicketMapper.selectByTicket("abcd");
        System.out.println(loginTicket);
    }

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessageMapper(){
        List<Message> messages = messageMapper.selectConversations(111,0,20);
        for (Message message : messages){
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
        System.out.println("-----------------------分界线-----------------------");
        List<Message> letters = messageMapper.selectLetters("111_112",0,10);
        for (Message letter : letters){
            System.out.println(letter);
        }
        System.out.println(letters.size());
        int letterCount = messageMapper.selectLetterCount("111_112");
        System.out.println(letterCount);
        System.out.println("-----------------------分界线-----------------------");
        int letterUnreadCount = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(letterUnreadCount);
    }
}
