package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList, Long> {
	public BlackList findByImei(Long imei);

	public BlackList save(BlackList blackList);

	public List<BlackList> getByImei(String imei);

	public void deleteByImei(String imei);

}

