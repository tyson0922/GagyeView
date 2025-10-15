package kopo.gagyeview.controller;

import kopo.gagyeview.service.RedisTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {
    @Autowired
    private RedisTestService redisTestService;

    @GetMapping("/redis-test/set")
    public String setTestKey(@RequestParam(defaultValue = "hello") String value) {
        redisTestService.setTestKey(value);
        return "Set test-key to: " + value;
    }

    @GetMapping("/redis-test/get")
    public String getTestKey() {
        String value = redisTestService.getTestKey();
        return "test-key value: " + value;
    }
}

