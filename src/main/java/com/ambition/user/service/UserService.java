package com.ambition.user.service;

import com.ambition.user.model.User;

import java.util.List;

/**
 * @author Elewin
 * 2019-05-15 11:19 PM
 */
public interface UserService {

    void insert(User record);

    void updateByPKSelective(User record);

    User getByPrimaryKey(Integer id);

    List<User> getActiveUsers();
}
