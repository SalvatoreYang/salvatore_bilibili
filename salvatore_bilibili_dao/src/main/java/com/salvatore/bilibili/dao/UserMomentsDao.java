package com.salvatore.bilibili.dao;

import com.salvatore.bilibili.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsDao {

    Integer addUserMoments(UserMoment userMoment);
}
