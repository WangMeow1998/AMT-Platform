package com.meow.community;

import com.meow.community.dao.DiscussPostMapper;
import com.meow.community.dao.UserMapper;
import com.meow.community.entity.DiscussPost;
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
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost discussPost : list){
            System.out.println(discussPost);
        }
    }
    @Test
    public void testSelectDiscussPostRows(){
        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }
}
