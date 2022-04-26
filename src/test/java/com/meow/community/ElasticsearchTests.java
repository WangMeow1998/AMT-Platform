package com.meow.community;


import com.alibaba.fastjson.JSONObject;
import com.meow.community.dao.DiscussPostMapper;
import com.meow.community.dao.elasticsearch.DiscussPostRepository;
import com.meow.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;


    //@Autowired  已弃用，改用下面的RestHighLevelClient
    //private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水。");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete(){
        //discussPostRepository.deleteById(231);
        //discussPostRepository.deleteAll();
    }

    //不带高亮的查询
    @Test
    public void noHighlightQuery() throws IOException {
        //discusspost为索引名
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                //依次按照type、score、createTime进行倒序排序
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)// 指定从哪条开始查询
                .size(10);// 需要查出的总记录条数

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> discussPostList = new ArrayList<>();

        for (SearchHit hit : searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            discussPostList.add(discussPost);
        }

        System.out.println(discussPostList.size());

        for (DiscussPost post : discussPostList){
            System.out.println(post);
        }
    }

    //带高亮的查询
    @Test
    public void highLightQuery() throws IOException {
        //discusspost是索引名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        Map<String,Object> res = new HashMap<>();
        
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span stype='color:read'>");
        highlightBuilder.postTags("</span>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                //依次按照type、score、createTime进行倒序排序
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)// 指定从哪条开始查询
                .size(10)// 需要查出的总记录条数
                .highlighter(highlightBuilder); //高亮

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> discussPostList = new ArrayList<>();

        long total = searchResponse.getHits().getTotalHits().value;

        for(SearchHit hit : searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            //处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null){
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if(contentField != null){
                discussPost.setContent(contentField.getFragments()[0].toString());
            }

            discussPostList.add(discussPost);
        }

        res.put("list", discussPostList);
        res.put("total", total);
        if(res.get("list")!= null){
            for (DiscussPost post : discussPostList = (List<DiscussPost>) res.get("list")) {
                System.out.println(post);
            }
            System.out.println(res.get("total"));
        }

    }
}
