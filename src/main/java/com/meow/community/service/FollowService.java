package com.meow.community.service;


import com.meow.community.entity.User;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followingKey = RedisKeyUtil.getFollowingKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().add(followingKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }
    public void unfollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followingKey = RedisKeyUtil.getFollowingKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().remove(followingKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    //查询某个用户的关注数
    public long findFollowingCount(int userId, int entityType){
        String followingKey = RedisKeyUtil.getFollowingKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followingKey);
    }

    //查询某个实体的粉丝数
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询某个实体的关注状态
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followingKey = RedisKeyUtil.getFollowingKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followingKey, entityId) != null;
    }

    //查询某个用户关注的所有人
    public List<Map<String, Object>> findFollowings(int userId, int offset, int limit){
        String followingKey = RedisKeyUtil.getFollowingKey(userId, ENTITY_TYPE_USER);
        //分页
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followingKey, offset, offset + limit - 1);
        if(targetIds == null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double time = redisTemplate.opsForZSet().score(followingKey, targetId);
            map.put("followingTime", new Date(time.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某个用户的所有粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        //分页
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if(targetIds == null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double time = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followerTime", new Date(time.longValue()));
            list.add(map);
        }
        return list;
    }
}
