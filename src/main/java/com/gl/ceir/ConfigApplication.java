package com.gl.ceir;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
//@EnableJpaAuditing
@EnableAutoConfiguration
// @EnableCaching
@EntityScan({"com.gl.ceir.config.model"})

@ComponentScan({"com.gl.ceir.config"})

@EnableEncryptableProperties


public class ConfigApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ConfigApplication.class, args);
    }


}
