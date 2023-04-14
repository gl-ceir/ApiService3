package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.DevBrandName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BrandRepository extends JpaRepository<DevBrandName, Long>, JpaSpecificationExecutor<DevBrandName> {

}
