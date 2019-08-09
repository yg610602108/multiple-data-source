package com.ambition.user.annotation;

import com.ambition.user.constant.DataSourceType;

import java.lang.annotation.*;

/**
 * Created by Elewin on 2019-05-15 8:13 PM
 * Description: 定义数据源注解，默认是 DataSourceType.DB_SLAVE
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataSource {

    String value() default DataSourceType.DB_SLAVE;
}
