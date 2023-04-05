package com.gl.ceir.config.repository.app;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gl.ceir.config.model.app.CustomDetails;

public interface CustomDetailsRepository extends JpaRepository<CustomDetails, Long> {
	
	public	CustomDetails save(CustomDetails customDetails);

}
