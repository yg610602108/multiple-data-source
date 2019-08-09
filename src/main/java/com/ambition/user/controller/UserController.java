package com.ambition.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.ambition.user.annotation.DataSource;
import com.ambition.user.constant.DataSourceType;
import com.ambition.user.model.User;
import com.ambition.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author Elewin
 * 2019-05-16 11:38 AM
 */
@Slf4j
@RestController
@RequestMapping(value = "user")
public class UserController {

    @Autowired
    private UserService userService;

    @DataSource(value = DataSourceType.DB_SLAVE)
    @GetMapping(value = "select")
    public String select() {
        User user = userService.getByPrimaryKey(1);
        return JSONObject.toJSONString(user);
    }

    @DataSource(value = DataSourceType.DB_MASTER)
    @GetMapping(value = "insert")
    public String insert() {
        User user = new User();
        user.setName("master");
        user.setPassword("123456");
        user.setActive(1);
        user.setStatus(1);
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());
        userService.insert(user);

        return "SUCCESS";
    }

    @DataSource(value = DataSourceType.DB_SLAVE)
    @GetMapping(value = "list")
    public String list() {
        List<User> activeUsers = userService.getActiveUsers();
        return JSONObject.toJSONString(activeUsers);
    }
}
