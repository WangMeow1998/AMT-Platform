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

    //验证码的前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";

    //登录凭证的前缀
    private static final String PREFIX_TICKET = "ticket";

    //用户缓存的前缀
    private static final String PREFIX_USER = "user";

    //独立访客的前缀
    private static final String PREFIX_UV = "uv";

    //日活跃用户的前缀
    private static final String PREFIX_DAU = "dau";

    //帖子的前缀
    private static final String PREFIX_POST = "post";

    //分数
    private static final String SCORE = "score";

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

    //登录验证码
    public static String getKaptchaKey(String kaptchaOwner){
        return PREFIX_KAPTCHA + SPLIT + kaptchaOwner;
    }

    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    //单日UV
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    //区间UV
    public static String getUVKey(String startDate, String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日活跃用户
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    //区间活跃用户
    public static String getDAUKey(String startDate, String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //帖子分数
    //post:score --> set(postId)
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + SCORE;
    }
}
