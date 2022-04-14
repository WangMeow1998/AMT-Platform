package com.meow.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }

    //MD5加密
    //hello -> abc123def456
    //hello + 3e4a8（salt） -> abc123def456abc
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //将数据转化为JSON格式，并返回提示码和提示信息
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }
    //方法重载
    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }
    //方法重载
    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }
    //测试
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("姓名","张三");
        map.put("年龄",23);
        String jsonStr = getJSONString(1,"消息",map);
        System.out.println(jsonStr);
    }
}
