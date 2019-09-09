package com.jcspider.server.component.redis;

import com.jcspider.server.component.ifc.JCLockTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;

/**
 * @author zhuang.hu Date:2019-07-04 Time:14:31
 */
public class RedisLockTool implements JCLockTool {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public boolean getLock(String key) {
        RedisConnection redisConnection = this.redisTemplate.getConnectionFactory().getConnection();
        String result = ((JedisCommands)redisConnection.getNativeConnection()).set(key, "lock", "NX", "PX", 1000);
        return "OK".equals(result);
    }

    @Override
    public void releaseLock(String key) {
    this.redisTemplate.delete(key);
    }
}
