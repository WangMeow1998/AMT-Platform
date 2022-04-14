package com.meow.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct //在Spring容器实例化这个Bean之后自动调用这个方法
    private void init(){
        try(
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ) {
            String keyword;
            while((keyword = reader.readLine()) != null){
                this.addKeyword(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }
    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        int len = keyword.length();
        for(int i = 0; i < len; i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){ //如果当前结点没有此结点
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //当前结点指向子节点，进入下一轮循环
            tempNode = subNode;
            //如果达到词尾，设置结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 需要过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1：指向树的结点
        TrieNode tempNode = rootNode;
        //指针2：指向文本的首部
        int begin = 0;
        //指针3：指向文本的尾部，可以前后移动
        int position = 0;
        //结果
        StringBuilder res = new StringBuilder();
        int textLen = text.length();
        while(position < textLen){
            char c = text.charAt(position);
            //如果是符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号记入结果，让指针2向下走一步
                if(tempNode == rootNode){
                    res.append(c);
                    begin++;
                }
                //无论符号在开头还是中间，指针3都向下走一步
                position++;
                continue;
            }
            //若不是符号
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){ //以begin开头的字符串不是敏感词
                res.append(text.charAt(begin));
                //移动指针2，进入下一个位置
                begin++;
                //指针3和指针2对齐
                position = begin;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()){ //发现敏感词，将begin ~ position字符串替换掉
                res.append(REPLACEMENT);
                //移动指针3，进入下一个位置
                position++;
                //指针2和指针3对齐
                begin = position;
                //重新指向根节点
                tempNode = rootNode;
            } else { //继续检查下一个字符（即：未检查到敏感词结尾）
                position++;
            }
        }
        //将最后一批不是敏感词的字符记入结果
        res.append(text.substring(begin));
        return res.toString();
    }

    /**
     * 判断是否为符号
     * 因为文本可能会出现符号，例如：★★开★票★★
     */
    private boolean isSymbol(Character c){
        //0x2E80 ~ 0x9FFF是东亚文字范围
        //如果字符（c不是英文字母和数字）并且（c不属于东亚文字字符），则返回true
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode{ //内部类
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点（key是下级字符，value是下级结点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }
        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
