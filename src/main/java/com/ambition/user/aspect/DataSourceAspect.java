package com.ambition.user.aspect;

import com.ambition.user.annotation.DataSource;
import com.ambition.user.common.DynamicDataSourceContextHolder;
import com.ambition.user.constant.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Elewin
 * 2019-05-15 10:33 PM
 */
@Slf4j
@Aspect
@Component
public class DataSourceAspect {

    /**
     * 定义切入点
     **/
    @Pointcut(value = "execution(* com.ambition.user.controller.*.*(..))")
    private void pointcut() { }

    @Before(value = "pointcut()")
    public void before(JoinPoint point) {
        // 切入点的方法名
        String method = point.getSignature().getName();
        // 方法参数类型数组
        Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();

        try {
            // 通过反射获取拦截的方法
            Method m = point.getSignature().getDeclaringType().getMethod(method, parameterTypes);
            // 方法不为空，并且加了数据源注解
            if (null != m && m.isAnnotationPresent(DataSource.class)) {
                // 由注解获取对应的数据源
                String dbType = m.getAnnotation(DataSource.class).value();
                // 注入数据源
                DynamicDataSourceContextHolder.putDataSource(dbType);
            }
        } catch (Exception ex) {
            log.error("inject data source failed, method:{}", method, ex);
        }
    }

    @After(value = "pointcut()")
    public void after() {
        DynamicDataSourceContextHolder.putDataSource(DataSourceType.DB_MASTER);
    }
}
