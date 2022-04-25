package com.meow.community.dao;


import com.meow.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //------------------------私信相关Start------------------------
    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信显示在页面
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话总数
    int selectConversationCount(int userId);

    //查询某个会话的所有私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话的私信总数
    int selectLetterCount(String conversationId);

    //查询未读私信总数
    int selectLetterUnreadCount(int userId, String conversationId);

    //增加私信
    int insertMessage(Message message);

    //更新私信的状态
    int updateMessageStatus(List<Integer> messageIds, int status);
    //------------------------私信相关End------------------------


    //------------------------通知相关Start------------------------
    //查询某个主题下的最新通知
    Message selectLatestNotice(int userId, String topic);

    //查询某个主题下包含的所有通知数量
    int selectNoticeCount(int userId, String topic);

    //查询某个主题下包含的所有未读通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    //查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
    //------------------------通知相关End------------------------
}
