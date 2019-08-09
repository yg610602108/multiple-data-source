package com.ambition.user.config;

import com.ambition.user.common.BasicDataSource;
import com.ambition.user.common.DynamicDataSource;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Elewin on 2019-05-15 7:53 PM
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {"com.ambition.user.dao"}, sqlSessionFactoryRef = "dynamicSqlSessionFactory")
public class DataSourceConfig {

    @Primary
    @Bean(name = "dynamicDataSource")
    public DataSource dataSource(@Qualifier(value = "db_master") BasicDataSource dataSourceMaster,
                                 @Qualifier(value = "db_slave") BasicDataSource dataSourceSlave) {
        Map<Object, Object> map = Maps.newHashMap();
        map.put("db_master", dataSourceMaster);
        map.put("db_slave", dataSourceSlave);

        DynamicDataSource dataSource = new DynamicDataSource();
        // 设置多数据源
        dataSource.setTargetDataSources(map);
        // 设置默认数据源
        dataSource.setDefaultTargetDataSource(dataSourceMaster);
        return dataSource;
    }

    /**
     * 配置主数据源
     **/
    @Bean(name = "db_master", destroyMethod = "close")
    public BasicDataSource dataSourceMaster(Environment environment) {
        String host = environment.getProperty("master.datasource.url");
        String username = environment.getProperty("master.datasource.name");
        String password = environment.getProperty("master.datasource.password");
        // 构造数据源
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(host);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * 配置从数据源
     **/
    @Bean(name = "db_slave", destroyMethod = "close")
    public BasicDataSource dataSourceSlave(Environment environment) {
        String host = environment.getProperty("slave.datasource.url");
        String username = environment.getProperty("slave.datasource.name");
        String password = environment.getProperty("slave.datasource.password");
        // 构造数据源
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(host);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "dynamicSqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier(value = "dynamicDataSource") DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        // 设置数据源
        sqlSessionFactoryBean.setDataSource(dataSource);

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 配置XML文件扫描路径
        Resource[] resources = resolver.getResources("classpath*:mapper/*.xml");

        sqlSessionFactoryBean.setMapperLocations(resources);
        return sqlSessionFactoryBean;
    }

    @Bean(name = "dynamicSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier(value = "dynamicSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "dynamicTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier(value = "dynamicDataSource") DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }
}
