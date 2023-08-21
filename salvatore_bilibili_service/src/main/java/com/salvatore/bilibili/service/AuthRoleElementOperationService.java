package com.salvatore.bilibili.service;

import com.salvatore.bilibili.dao.AuthRoleElementOperationDao;
import com.salvatore.bilibili.domain.auth.AuthRoleElementOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleElementOperationService {

    @Autowired
    private AuthRoleElementOperationDao authRoleElementOperationDao;

    public List<AuthRoleElementOperation> getAuthRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationDao.getAuthRoleElementOperationsByRoleIds(roleIdSet);
    }
}
