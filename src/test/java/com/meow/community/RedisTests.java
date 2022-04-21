package com.meow.community;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 233);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }

    @Test
    public void testHashs(){
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey,"username","张三");
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";

        redisTemplate.opsForList().rightPush(redisKey, 101);
        redisTemplate.opsForList().rightPush(redisKey, 102);
        redisTemplate.opsForList().rightPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
    }

    @Test
    public void testSets(){
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey,"刘备","关羽","张飞");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSets(){
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey,"宋江",1);
        redisTemplate.opsForZSet().add(redisKey,"吴用",3);
        redisTemplate.opsForZSet().add(redisKey,"卢俊义",2);

        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));
    }

    //多次访问同一个key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        System.out.println(operations.get());
        operations.increment();
        System.out.println(operations.get());
    }
    //编程式事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi(); //启动事务

                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");

                System.out.println(operations.opsForSet().members(redisKey)); //在事务期间访问数据，是访问不到的

                return operations.exec(); //提交事务
            }
        });
        System.out.println(obj);
    }
}
