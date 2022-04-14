package com.meow.community.dao;

import com.meow.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //userId为0则查询所有用户，userId不为0查询特定用户
    //offset表示第几页
    //limit表示分页后，每页显示多少条帖子
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //获取帖子的数量
    //@Param注解用于给参数取别名
    //如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    //发布帖子
    int insertDiscussPost(DiscussPost discussPost);
}
