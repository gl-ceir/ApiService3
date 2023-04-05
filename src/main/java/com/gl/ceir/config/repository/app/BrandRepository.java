package com.gl.ceir.config.repository.app;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.gl.ceir.config.model.app.BrandName;
import org.springframework.stereotype.Repository;


@Repository
public interface BrandRepository  extends JpaRepository<BrandName, Long>, JpaSpecificationExecutor<BrandName>  {
	
}
