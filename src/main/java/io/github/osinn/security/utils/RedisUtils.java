package io.github.osinn.security.utils;

import io.github.osinn.security.service.IRedissonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wency_cai
 **/
@Slf4j
@Component
@SuppressWarnings({"unchecked", "all"})
public class RedisUtils {

    private IRedissonService redissonService;

    public RedisUtils(IRedissonService redissonService) {
        this.redissonService = redissonService;
    }

    // =============================common============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redissonService.getRedissonClient().getBucket(key).expire(time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 根据 key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效, 返回-2代表不存在
     */
    public long getExpire(String key) {
        return redissonService.getRedissonClient().getBucket(key).getExpireTime();
    }

    /**
     * 查找匹配key
     *
     * @param pattern key
     * @return /
     */
    public List<String> scan(String pattern) {
        Iterable<String> keysByPattern = redissonService.getRedissonClient().getKeys().getKeysByPattern(pattern);
        List<String> keys = new ArrayList<>();
        for (String key : keysByPattern) {
            keys.add(key);
        }
        return keys;
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redissonService.delete(key[0]);
            } else {
                redissonService.getRedissonClient().getKeys().delete(key);
            }
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public <T> T get(String key, Class<T> clazz) {
        return redissonService.getValue(key);
    }


    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        redissonService.setValue(key, value);
        return true;
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        redissonService.setValue(key, value, time);
        return true;
    }

    // ===============================list=================================

    /**
     * 根据前缀删除缓存
     *
     * @param prefix 前缀
     */
    public void deleteCacheByPrefix(String prefix) {
        List<String> list = scan(prefix + "*");
        if (!list.isEmpty()) {
            String[] array = new String[list.size()];
            array = list.toArray(array);
            redissonService.getRedissonClient().getKeys().delete(array);
        }
    }

    /**
     * 根据前缀删除缓存
     *
     * @param prefix 前缀
     */
    public <T> List<T> fetchLike(String prefix) {
        List<String> keys = scan(prefix);
        List<T> list = new ArrayList<>();
        for (String key : keys) {
            list.add(redissonService.getValue(key));
        }
        list.removeIf(x -> x == null);
        return list;
    }
}
