package com.meow.community.service;

import com.meow.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class AlphaService {
    public AlphaService(){
        System.out.println("实例化AlphaService");
    }
    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService Bean");
    }
    @PreDestroy
    public void destory(){
        System.out.println("销毁AlphaService Bean");
    }

    @Autowired
    private AlphaDao alphaDao; //service依赖于dao
    public String find(){
        return alphaDao.select();
    }
}
