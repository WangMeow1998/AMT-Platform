package com.meow.community.service;


import com.alibaba.fastjson.JSONObject;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    //Elasticsearch存储帖子
    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    //Elasticsearch删除帖子
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    //Elasticsearch搜索帖子
    public Map<String, Object> searchDiscussPost(String keyword, int current, int limit) throws IOException {

        //discusspost是索引名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        Map<String, Object> res = new HashMap<>();

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                //依次按照type、score、createTime进行倒序排序
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(current)// 指定从哪条开始查询
                .size(limit)// 需要查出的总记录条数(每页最大数量)
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


        return res;
        //if(res.get("list")!= null){
        //    for (DiscussPost post : discussPostList = (List<DiscussPost>) res.get("list")) {
        //        System.out.println(post);
        //    }
        //    System.out.println(res.get("total"));
        //}
    }

}
