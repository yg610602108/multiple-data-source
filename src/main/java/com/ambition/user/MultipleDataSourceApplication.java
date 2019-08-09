package com.ambition.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Author: Elewin
 * Date: 2019-02-06 10:31 PM
 * Project: microservice
 * Package: com.ambition.user
 * Description: TODO
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MultipleDataSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleDataSourceApplication.class, args);
    }
}
