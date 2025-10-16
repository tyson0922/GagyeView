package kopo.gagyeview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "kopo.gagyeview.feign")
@MapperScan("kopo.gagyeview.persistence.mapper")
public class GagyeViewApplication {

    public static void main(String[] args) {
        SpringApplication.run(GagyeViewApplication.class, args);
    }

}
