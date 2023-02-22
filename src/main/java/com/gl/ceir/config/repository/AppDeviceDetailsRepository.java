package com.gl.ceir.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.AppDeviceDetailsDb;

public interface AppDeviceDetailsRepository extends JpaRepository<AppDeviceDetailsDb, Long>, JpaSpecificationExecutor<AppDeviceDetailsDb> {

    public AppDeviceDetailsDb save(AppDeviceDetailsDb appDeviceDetailsDb);

}
