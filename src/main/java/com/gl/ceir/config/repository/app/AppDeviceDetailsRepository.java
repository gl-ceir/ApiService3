package com.gl.ceir.config.repository.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.app.AppDeviceDetailsDb;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppDeviceDetailsRepository extends JpaRepository<AppDeviceDetailsDb, Long>, JpaSpecificationExecutor<AppDeviceDetailsDb> {

    @Modifying
    @Query(value = "insert into mobile_app_dev_info (os_type,device_id,device_details,language_type)   VALUES (:osType,:deviceId  ,:deviceDetails , :languageType)", nativeQuery = true)
    @Transactional
    void saveDetails(@Param("osType") String osType, @Param("deviceId") String deviceId, @Param("deviceDetails") String deviceDetails, @Param("languageType") String languageType);

}
