package com.gl.ceir.config;

import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.gl.ceir.config.configuration.FileStorageProperties;
import com.gl.ceir.config.model.app.DeviceSnapShot;
import com.gl.ceir.config.model.app.DuplicateImeiMsisdn;
import com.gl.ceir.config.model.app.ImeiMsisdnIdentity;
import com.gl.ceir.config.model.constants.ImeiStatus;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaAuditing
@EnableAutoConfiguration
@EnableCaching
@EntityScan({"com.gl.ceir.config.audit.model"})
@ComponentScan({"com.gl.ceir.config"})

@EnableEncryptableProperties
public class ConfigApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ConfigApplication.class, args);
    }

    private static DeviceSnapShot convertRequestToDeviceSnapShot() {
        DeviceSnapShot deviceSnapShot = new DeviceSnapShot();
        deviceSnapShot.setImei(898989L);
        // deviceSnapShot.setFailedRuleId(request.getFailRule().getId().toString());
        // deviceSnapShot.setFailedRuleName(request.getFailRule().getName());
        deviceSnapShot.setDuplicateImeiMsisdns(new ArrayList<>());
        deviceSnapShot.getDuplicateImeiMsisdns().add(convertToDuplicateImeiMsisdn());
        deviceSnapShot.getDuplicateImeiMsisdns().get(0).setDeviceSnapShot(deviceSnapShot);
        return deviceSnapShot;
    }

    private static DuplicateImeiMsisdn convertToDuplicateImeiMsisdn() {
        DuplicateImeiMsisdn duplicateImeiMsisdn = new DuplicateImeiMsisdn();
        duplicateImeiMsisdn.setImeiMsisdnIdentity(new ImeiMsisdnIdentity(898989L, 9090909L));
        duplicateImeiMsisdn.setFileName("file");

        duplicateImeiMsisdn.setImeiStatus(ImeiStatus.AUTO_REGULARIZED);
        duplicateImeiMsisdn.setImsi(3232L);
        duplicateImeiMsisdn.setRegulizedByUser(Boolean.FALSE);
        return duplicateImeiMsisdn;

    }

}
