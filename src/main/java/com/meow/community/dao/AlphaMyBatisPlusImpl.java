package com.meow.community.dao;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaMyBatisPlusImpl implements AlphaDao{
    @Override
    public String select() {
        return "MyBatisPlus";
    }
}
