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
     *实体类型：帖子的评论
     */
    int ENTITY_TYPE_POST_COMMENT = 1;

    /**
     * 实体类型：评论的评论，即回复
     */
    int ENTITY_TYPE_COMMENT_REPLY = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;
}
