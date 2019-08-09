package com.ambition.user.dao.proxy;

import com.ambition.user.cache.GenericCacheHelper;
import com.ambition.user.cache.JavaMemcachedClient;
import com.ambition.user.dao.UserDao;
import com.ambition.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Elewin
 * 2019-05-16 4:31 PM
 */
@Slf4j
@Repository
public class UserDaoProxy implements UserDao {

    private static final String PREFIX_VERSION = "version_user";
    private static final String PREFIX_OBJECT = "object_user";
    private static final String PREFIX_LIST = "list_user";
    private static final int EXPIRED_THREE_HOUR = 3 * 60 * 60;

    @Autowired
    private UserDao userDao;
    @Autowired
    private JavaMemcachedClient cacheClient;

    private long getVersion() {
        return GenericCacheHelper.getVersion(PREFIX_VERSION, cacheClient);
    }

    private String genKeyById(int id) {
        return GenericCacheHelper.genKeyById(PREFIX_OBJECT, id);
    }

    @Override
    public int deleteByPrimaryKey(Integer id) {
        int result = userDao.deleteByPrimaryKey(id);
        if(result > 0) {
            // 删除缓存对象
            GenericCacheHelper.delete(genKeyById(id), cacheClient);
            // 修改版本号
            GenericCacheHelper.increaseVersion(PREFIX_VERSION, cacheClient);
        }
        return result;
    }

    @Override
    public int insert(User record) {
        int result = userDao.insert(record);
        if (result > 0) {
            // 修改版本号，使集合缓存失效
            GenericCacheHelper.increaseVersion(PREFIX_VERSION, cacheClient);
        }
        return result;
    }

    @Override
    public User selectByPrimaryKey(Integer id) {
        String key = genKeyById(id);
        // 从缓存中获取
        User record = (User) cacheClient.get(key);
        if(null == record) {
            log.info("load user from db, userId:{}", id);
            // 从数据库获取
            record = userDao.selectByPrimaryKey(id);
            // 存入缓存
            GenericCacheHelper.set(key, record, EXPIRED_THREE_HOUR, cacheClient);
        }
        return record;
    }

    @Override
    public int updateByPrimaryKeySelective(User record) {
        int result = userDao.updateByPrimaryKeySelective(record);
        if(result > 0) {
            // 删除缓存对象
            GenericCacheHelper.delete(genKeyById(record.getId()), cacheClient);
            // 修改版本号，使集合缓存失效
            GenericCacheHelper.increaseVersion(PREFIX_VERSION, cacheClient);
        }
        return result;
    }

    @Override
    public List<User> selectActiveRecords() {
        long version = getVersion();
        String key = GenericCacheHelper.genKeyWithVersion(PREFIX_LIST, version, "Active", "1");
        // 从缓存中获取数据
        List<User> list = (List<User>) cacheClient.get(key);

        if (null == list || list.isEmpty()) {
            log.info("load selectActiveRecords from db, table name:{}", "t_user");
            list = userDao.selectActiveRecords();
            // 存入缓存
            GenericCacheHelper.set(key, list, cacheClient);
        }
        return list;
    }
}
