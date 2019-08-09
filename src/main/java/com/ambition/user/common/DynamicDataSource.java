package com.ambition.user.common;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author Elewin
 * 2019-05-15 10:11 PM
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDaraSource();
    }
}
