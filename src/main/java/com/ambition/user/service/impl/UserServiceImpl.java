package com.ambition.user.service.impl;

import com.ambition.user.dao.proxy.UserDaoProxy;
import com.ambition.user.model.User;
import com.ambition.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Elewin
 * 2019-05-16 11:37 AM
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDaoProxy userDao;

    @Override
    public void insert(User record) {
        userDao.insert(record);
    }

    @Override
    public void updateByPKSelective(User record) {
        userDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public User getByPrimaryKey(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @Override
    public List<User> getActiveUsers() {
        return userDao.selectActiveRecords();
    }
}
