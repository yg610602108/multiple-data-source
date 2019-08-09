package com.ambition.user.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Elewin
 * 2019-05-16 4:38 PM
 */
@Slf4j
public class GenericCacheHelper {

    private static final int CACHE_TIME_DEFAULT = 4 * 60 * 60; // 4 hours
    private static final int CACHE_TIME_VERSION = 0; // never expire
    private static final long VERSION_STEP = 1;

    private static Object SHARE_OBJECT = new Object();

    private static LoadingCache<String, Object> MUTEX = CacheBuilder.newBuilder()
            .maximumSize(10_000_000)
            .expireAfterWrite(1L, TimeUnit.HOURS)
            .build(new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) {
                    return new Object();
                }
            });

    /**
     * 根据key获取对象锁，用以控制访问数据库的并发
     *
     * @param key 同cache的key
     * @return 对象锁
     */
    public static Object getMutex(String key) {
        try {
            return MUTEX.get(key);
        } catch (ExecutionException e) {
            return SHARE_OBJECT;
        }
    }


    /**
     * 缓存一个对象。如果对象为null，不会操作缓存服务器。
     */
    public static void set(String key, Object val, JavaMemcachedClient cacheClient) {
        set(key, val, CACHE_TIME_DEFAULT, cacheClient);
    }

    /**
     * 清除一个缓存
     */
    public static boolean delete(String key, JavaMemcachedClient cacheClient) {
        return cacheClient.delete(key);
    }

    /**
     * 缓存一个对象。如果对象为null，不会操作缓存服务器。
     *
     * @param key
     * @param val
     * @param expireSec   超时时间，单位：秒。为0则永不过期。
     * @param cacheClient
     */
    public static void set(String key, Object val, int expireSec, JavaMemcachedClient cacheClient) {
        if (null != val) {
            cacheClient.set(key, expireSec, val);
        }
    }

    /**
     * 增加指定的版本号。如果不存在，则设置为1.
     *
     * @return 增加之后的版本号
     */
    public static long increaseVersion(String key, JavaMemcachedClient cacheClient) {
        return cacheClient.incrCounter(key, CACHE_TIME_VERSION, VERSION_STEP);
    }

    /**
     * 获取指定的版本号。如果不存在，返回-1.
     */
    public static long getVersion(String key, JavaMemcachedClient cacheClient) {
        return cacheClient.getCounter(key);
    }

    /**
     * Generate key for an object by id, Example: city[id=21]
     */
    public static String genKeyById(String objectName, long id) {
        return genKey(objectName, "id", Long.toString(id));
    }

    /**
     * Generate key for an object, Example: city[id=21]
     */
    public static String genKey(String objectName, String key, String value) {
        return objectName + "[" + key + "=" + value + "]";
    }

    /**
     * Generate key with version. Example: cities-v21[active=Y]
     */
    public static String genKeyWithVersion(String name, long version, String key, String value) {
        return name + "-v" + Long.toString(version) + "[" + key + "=" + value + "]";
    }

    /**
     * Generate key with version and multiple parameters. Example: zones-v21[active=Y,cityId=1].
     * <p>
     * 方括号中的key-value pair, 按照key的字母顺序排列
     * <p>
     * TODO 如果传入的map中，value为数组，例如int[]，需要做处理，否则在结果字符串中会使用内存地址作为value
     *
     * @param name
     * @param version
     * @param params
     * @return
     */
    public static String genKeyWithVersion(String name, long version, Map<String, Object> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String result = name + "-v" + Long.toString(version) + "[";
        if (keys.size() > 0) {
            for (String key : keys) {
                result += key + "=" + objectToString(params.get(key)) + ",";
            }
            result = result.substring(0, result.length() - 1);
        }
        result += "]";
        return result;
    }

    /**
     * 将Object类型转成String类型，特别处理了当Object值为数组类型时的情况
     * 如果是数组的情况，将数组元素组装成【1,2,3】的格式返回
     * 特殊情况，假如是非数据类型对象或对象数组，需要在该对象中重写toString方法，否则将返回对象的内存地址;
     * object为null时，返回空
     * 此方法暂没有处理数组元素的排序问题，如[1,2,3]和[1,3,2]的情况，返回的值不同，若需要返回值相同，需要特别处理
     */
    public static String objectToString(Object object) {
        if (null == object || StringUtils.isBlank(object.toString())) {
            return "";
        }
        if (object.getClass().isArray()) {
            // 数组类型，需要特别处理，否则toString只能返回内存地址
            // 基本数据类型不能直接转成Object数组，需要对基本数据类型特别处理
            // 引用类型直接转成Object数组来处理
            String value = "[";
            if (object instanceof int[]) {
                int[] ints = (int[]) object;
                for (int i : ints) {
                    value += i + ",";
                }
            } else if (object instanceof long[]) {
                long[] longs = (long[]) object;
                for (long l : longs) {
                    value += l + ",";
                }
            } else if (object instanceof short[]) {
                short[] shorts = (short[]) object;
                for (short s : shorts) {
                    value += s + ",";
                }
            } else if (object instanceof float[]) {
                float[] floats = (float[]) object;
                for (float f : floats) {
                    value += f + ",";
                }
            } else if (object instanceof double[]) {
                double[] doubles = (double[]) object;
                for (double d : doubles) {
                    value += d + ",";
                }
            } else if (object instanceof char[]) {
                char[] chars = (char[]) object;
                for (char c : chars) {
                    value += c + ",";
                }
            } else if (object instanceof byte[]) {
                byte[] bytes = (byte[]) object;
                for (byte b : bytes) {
                    value += b + ",";
                }
            } else if (object instanceof boolean[]) {
                boolean[] booleans = (boolean[]) object;
                for (boolean b : booleans) {
                    value += b + ",";
                }
            } else {
                Object[] objects = (Object[]) object;
                for (Object obj : objects) {
                    value += obj == null ? "" : obj.toString() + ",";
                }
            }
            return value.substring(0, value.length() - 1) + "]";
        } else {
            // 不是数组类型，直接返回toString
            return object.toString();
        }
    }
}
