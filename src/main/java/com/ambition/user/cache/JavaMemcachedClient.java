package com.ambition.user.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Elewin
 * 2019-05-16 4:11 PM
 */
@Slf4j
public class JavaMemcachedClient {

    /**
     * 同步方法的超时时间
     **/
    private static final int SYNC_TIMEOUT = 50;

    private ThreadFactory factory = new ThreadFactoryBuilder()
            .setNameFormat("NotifierMemcachedListener").build();

    private MemcachedClient client;
    private String ipPorts;
    private String userName;
    private String password;

    public JavaMemcachedClient(String ipPorts, String userName, String password) {
        this.ipPorts = ipPorts;
        this.userName = userName;
        this.password = password;
        init();
    }

    private void init() {
        try {
            // 服务器列表
            List<String> servers = Arrays.asList(ipPorts.split(","));

            ExecutorService executorService = new ThreadPoolExecutor(
                    5, 10,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024),
                    factory,
                    new ThreadPoolExecutor.AbortPolicy()
            );

            // ExecutorService executorService = Executors.newFixedThreadPool(4, r -> new Thread(r, "NotifierMemcachedListener"));

            ConnectionFactoryBuilder factoryBuilder = new ConnectionFactoryBuilder()
                    .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);

            if (StringUtils.isNotBlank(userName)) {
                AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"},
                        new PlainCallbackHandler(userName, password)
                );
                factoryBuilder.setAuthDescriptor(ad);
            }

            factoryBuilder.setListenerExecutorService(executorService);
            factoryBuilder.setTimeoutExceptionThreshold(50);
            // 初始化
            client = new MemcachedClient(factoryBuilder.build(), AddrUtil.getAddresses(servers));
        } catch (Exception ex) {
            log.error("Failed to init memcached client. ");
            throw new IllegalStateException("Failed to init memcached client.", ex);
        }
    }

    public void shutdown() {
        log.info("start to shutdown memcached.");
        if (client != null) {
            client.shutdown();
        }
    }

    public boolean add(String key, int expireSec, Object val) {
        try {
            client.add(key, expireSec, val);
        } catch (Exception ex) {
            log.error("memcached fail to add, key=" + key, ex);
        }
        return true;
    }

    public boolean addOfSync(String key, int expireSec, Object val) {
        try {
            client.add(key, expireSec, val).get(SYNC_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            log.error("memcached fail to add, key=" + key, ex);
        }
        return true;
    }

    public boolean delete(String key) {
        try {
            client.delete(key);
        } catch (Exception ex) {
            log.error("memcached fail to delete, key=" + key, ex);
        }
        return true;
    }

    public boolean deleteOfSync(String key) {
        try {
            client.delete(key).get(SYNC_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            log.error("memcached fail to delete, key=" + key, ex);
        }
        return true;
    }

    /**
     * 缓存一个对象，并设置过期时间。如果key已存在，则覆盖。
     *
     * 由于set操作实际是个异步操作，等待操作结果会block客户端线程。故此设计为不检查操作结果，直接返回true。
     * （遇到Exception时为false）
     *
     * @param key 对象唯一标识
     * @param expireSec 0:永不过期；1-60*60*24*30:多少秒后过期；大于60*60*24*30:绝对时间
     * @param val 缓存数据
     * @return boolean
     */
    public boolean set(String key, int expireSec, Object val) {
        try {
            client.set(key, expireSec, val);
            return true;
        } catch (Exception ex) {
            log.error("memcached fail to set, key=" + key, ex);
            return false;
        }
    }

    public boolean setOfSync(String key, int expireSec, Object val) {
        try {
            client.set(key, expireSec, val).get(SYNC_TIMEOUT, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception ex) {
            log.error("memcached fail to set, key=" + key, ex);
            return false;
        }
    }

    /**
     * 根据指定的key，从服务器获缓存的对象。如果不存在，返回null。
     *
     * 如果发生Exception，也返回null.
     */
    public Object get(String key) {
        Object result = null;
        try {
            result = client.get(key);
        } catch (Exception ex) {
            log.error("memcached fail to get, key=" + key, ex);
        }
        if (result == null) {
            log.debug("memcached get request missing for key : {}", key);
        }
        return result;
    }

    /**
     * 一次性根据多个key，从服务器获取缓存的多个对象。
     *
     * 如果发生Exception，返回一个没有元素的Map.
     */
    public Map<String, Object> getMulti(String[] keys) {
        try {
            return client.getBulk(keys);
        } catch (Exception ex) {
            log.error("memcached fail to getMulti, keys=" + keys, ex);
        }
        return new HashMap<String, Object>();
    }

    /**
     * 清除所有缓存
     */
    public boolean flush() {
        try {
            client.flush();
        } catch (Exception ex) {
            log.error("memcached fail to flush", ex);
        }
        return true;
    }

    /**
     * 获取计数器当前的值
     *
     * @param key
     * @return 如果计数器不存在，或者遇到Exception，返回-1.
     */
    public long getCounter(String key) {
        try {
            String strValue = (String) client.get(key);
            long counter = null == strValue ? -1 : NumberUtils.toLong(StringUtils.trim(strValue));
            return counter;
        } catch (Exception ex) {
            log.error("memcached fail to get counter, key=" + key, ex);
            return -1;
        }
    }

    /**
     * 设置计数器的值。如果计数器不存在，则创建之。
     *
     * @param key
     * @param expireSec
     * @param val       计数器初始值，必须为正整数。
     * @return
     */
    public boolean setCounter(String key, int expireSec, long val) {
        if (val < 0) {
            throw new IllegalArgumentException("Count cannot be negetive.");
        }
        try {
            return set(key, expireSec, String.valueOf(val));
        } catch (Exception ex) {
            log.error("memcached fail to setCounter, key=" + key + ", expireSec=" + expireSec + ", val=" + val, ex);
        }
        return true;
    }

    public boolean setCounterOfSync(String key, int expireSec, long val) {
        if (val < 0) {
            throw new IllegalArgumentException("Count cannot be negetive.");
        }
        try {
            return setOfSync(key, expireSec, String.valueOf(val));
        } catch (Exception ex) {
            log.error("memcached fail to setCounter, key=" + key + ", expireSec=" + expireSec + ", val=" + val, ex);
        }
        return true;
    }

    /**
     * @param key
     * @param by  增加的数值，必须为正整数
     * @return 如果计数器不存在，或者遇到Exception，返回-1.
     */
    public long incrCounter(String key, long by) {
        try {
            return client.incr(key, by);
        } catch (Exception ex) {
            log.error("memcached fail to incrCounter, key=" + key + ", by=" + by, ex);
        }
        return -1;
    }

    /**
     * 给计数器增加一个值。如果计数器不存在，则创建之，并把计数器的值设置为指定的值。
     *
     * @param key
     * @param expireSec
     * @param by        增加的数值，必须为正整数
     * @return
     */
    public long incrCounter(String key, int expireSec, long by) {
        long count = incrCounter(key, by);
        if (count > 0) {
            return count;
        }
        setCounter(key, expireSec, by);
        return 1;
    }

    /**
     * @param key
     * @param by  减少的数值
     * @return 如果计数器不存在，或者遇到Exception，返回-1.
     */
    public long decrCounter(String key, long by) {
        try {
            return client.decr(key, by);
        } catch (Exception ex) {
            log.error("memcached fail to decrCounter, key=" + key + ", by=" + by, ex);
            return -1;
        }
    }
}
