package com.meow.community.service;

import com.meow.community.dao.AlphaDao;
import com.meow.community.dao.DiscussPostMapper;
import com.meow.community.dao.UserMapper;
import com.meow.community.entity.DiscussPost;
import com.meow.community.entity.User;
import com.meow.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {
    public AlphaService(){
//        System.out.println("实例化AlphaService");
    }
    @PostConstruct
    public void init(){
//        System.out.println("初始化AlphaService Bean");
    }
    @PreDestroy
    public void destory(){
//        System.out.println("销毁AlphaService Bean");
    }

    @Autowired
    private AlphaDao alphaDao; //service依赖于dao
    public String find(){
        return alphaDao.select();
    }


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private final static Logger logger = LoggerFactory.getLogger(AlphaService.class);

    //isolations: 事务的隔离级别
    //propagation: 传播机制
    //Propagation.REQUIRED: 支持当前事务（外部事物，举例：两个事务A、B，A调用B，则A是B的外部事物，B事务会跟随A事务），如果不存在则创建新事物。
    //Propagation.REQUIRES_NEW: 创建一个新事物，并且暂停当前事务（外部事物）。
    //Propagation.NESTED: 如果当前存在事务（外部事物），则嵌套在该事务中执行（独立的提交和回滚），否则就会和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1(){
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("wangmeow1998@163.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello");
        discussPost.setContent("新人报道");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        Integer.valueOf("abc"); //故意造成异常

        //发生异常后，事务会进行回滚，不会插入用户和帖子
        return "ok";
    }

    @Autowired
    private TransactionTemplate transactionTemplate;
    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("wangmeow1998@163.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
                //新增帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("hello");
                discussPost.setContent("新人报道");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);

                Integer.valueOf("abc"); //故意造成异常

                //发生异常后，事务会进行回滚，不会插入用户和帖子
                return "ok";
            }
        });
    }


    //被@Async修饰的方法，在多线程环境下会被异步调用
    @Async
    public void execute1(){
        logger.debug("@Async注解修饰的方法，可被多线程执行");
    }

    ////会被自动扫描到，不用调用这个方法，会被自动调用
    //@Scheduled(initialDelay = 10000, fixedRate = 1000)
    //public void execute2(){
    //    logger.debug("@Scheduled注解修饰的方法，可被多线程执行");
    //}
}
