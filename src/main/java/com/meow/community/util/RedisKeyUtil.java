package com.meow.community.util;



public class RedisKeyUtil {
    private static final String SPLIT = ":";

    //点赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    //记录用户获得点赞数量的Key的前缀
    private static final String PREFIX_USER_LIKE = "like:user";

    //用户关注实体的Key的前缀
    private static final String PREFIX_FOLLOWING = "following";

    //实体的粉丝的Key的前缀
    private static final String PREFIX_FOLLOWER = "follower";

    //某个实体的赞
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //用户获得的点赞数量
    //like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    //following:userId:entityType -> zset(entityId,nowTime)
    public static String getFollowingKey(int userId, int entityType){
        return PREFIX_FOLLOWING + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体的粉丝
    //follower:entityType:entityId -> zset(userId,nowTime)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
