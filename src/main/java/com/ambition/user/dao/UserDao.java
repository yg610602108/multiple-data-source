package com.ambition.user.dao;

import com.ambition.user.model.User;

import java.util.List;

/**
 * @author Elewin
 * 2019-05-15 11:13 PM
 */
public interface UserDao {

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    List<User> selectActiveRecords();
}
