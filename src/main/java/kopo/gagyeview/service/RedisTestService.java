package kopo.gagyeview.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setTestKey(String value) {
        redisTemplate.opsForValue().set("test-key", value);
    }

    public String getTestKey() {
        return redisTemplate.opsForValue().get("test-key");
    }
}

