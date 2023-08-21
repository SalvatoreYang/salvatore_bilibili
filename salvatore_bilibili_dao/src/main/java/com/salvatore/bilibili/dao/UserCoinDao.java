package com.salvatore.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface UserCoinDao {

    Integer getUserCoinsAmount(Long userId);

    Integer updateUserCoinsAmount(@Param("userId") Long userId,
                                  @Param("amount") Integer amount,
                                  @Param("updateTime") Date updateTime);
}
