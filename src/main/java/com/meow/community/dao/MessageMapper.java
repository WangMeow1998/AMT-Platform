package com.meow.community.dao;


import com.meow.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

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
}
