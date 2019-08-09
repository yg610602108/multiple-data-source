package com.ambition.user.common;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import java.sql.SQLException;

/**
 * @author Elewin
 * 2019-05-15 10:00 PM
 */
public class BasicDataSource extends DruidDataSource {

    private static final long serialVersionUID = -2389366247852282674L;

    public BasicDataSource() {
        Environment environment = new StandardEnvironment();
        // 数据库驱动
        String diverClassName = environment.getProperty("spring.datasource.driver-class-name");
        // 构造数据源
        this.setDriverClassName(diverClassName);
        this.setInitialSize(5);
        this.setMinIdle(3);
        this.setMaxActive(20);
        this.setMaxWait(10000);
        this.setValidationQuery("select 1 from dual");
        this.setTestWhileIdle(true);
        this.setTimeBetweenEvictionRunsMillis(60000);
        this.setMinEvictableIdleTimeMillis(300000);
        this.setPoolPreparedStatements(false);
        this.setMaxPoolPreparedStatementPerConnectionSize(20);
        this.setLogAbandoned(true);
        this.setRemoveAbandoned(true);
        this.setRemoveAbandonedTimeout(180);
        try {
            this.setFilters("stat");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
