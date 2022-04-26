package com.meow.community.util;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认用户状态的持续时间
     */
    int DEFAULT_EXPIRED_TIME = 60 * 60 * 12; //单位：秒，默认为12个小时

    /**
     * 记住用户状态的持续时间
     */
    int REMEMBER_EXPIRED_TIME = 60 * 60 * 24 * 90; ////单位：秒，勾选记住时，状态为3个月

    /**
     *实体类型：帖子
     */

    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";


    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";


    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 系统管理员ID
     */
    int SYSTEM_USER_ID = 1;
}
