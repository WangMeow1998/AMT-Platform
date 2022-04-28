package com.meow.community;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.StandardCharsets;

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

    @Test
    public void testHyperLoglog(){
        String redisKey = "test:hll:01";

        for(int i = 0; i < 10000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for(int i = 0; i < 10000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    @Test
    public void testBitmap(){
        String redisKey1 = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey1, 0, true);
        //redisTemplate.opsForValue().setBit(redisKey1, 1, true);
        //redisTemplate.opsForValue().setBit(redisKey1, 2, true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey1,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey1,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey1,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey1,3));


        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey1.getBytes());
            }
        });
        System.out.println(obj);


        String redisKey2 = "test:bm:02";
        //redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        //redisTemplate.opsForValue().setBit(redisKey2, 2, true);


        String redisKey3 = "test:bm:03";
        //redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        //redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);

        Object obj2 = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey4 = "test:bm:res";
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey4.getBytes(), redisKey1.getBytes(), redisKey2.getBytes(), redisKey3.getBytes());

                return connection.bitCount(redisKey4.getBytes());
            }
        });

        System.out.println(obj2);
    }
}
