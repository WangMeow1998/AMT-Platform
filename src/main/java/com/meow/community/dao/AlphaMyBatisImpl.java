package com.meow.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphaMyBatis")
public class AlphaMyBatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
