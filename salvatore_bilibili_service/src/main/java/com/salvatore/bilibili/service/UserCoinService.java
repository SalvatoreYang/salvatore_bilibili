package com.salvatore.bilibili.service;

import com.salvatore.bilibili.dao.UserCoinDao;
import com.salvatore.bilibili.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserCoinService {
    @Autowired
    private UserCoinDao userCoinDao;

    public Integer getUserCoinsAmount(Long userId) {
        return userCoinDao.getUserCoinsAmount(userId);
    }

    public void updateUserCoinsAmount(Long userId, Integer amount) {
        Date updateTime = new Date();
        userCoinDao.updateUserCoinsAmount(userId, amount, updateTime);
    }

}
