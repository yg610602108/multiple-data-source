package com.ambition.user.config;

import com.ambition.user.cache.JavaMemcachedClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Elewin
 * 2019-05-16 4:10 PM
 */
@Slf4j
@Configuration
public class MemcachedConfig {

    @Bean(destroyMethod = "shutdown")
    public JavaMemcachedClient memcachedClient(Environment environment) {
        String ipPorts = environment.getProperty("memcached.host");
        String userName = environment.getProperty("memcached.userName");
        String password = environment.getProperty("memcached.password");
        return new JavaMemcachedClient(ipPorts, userName, password);
    }
}
