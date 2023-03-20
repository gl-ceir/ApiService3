package com.gl.ceir.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.GsmaTacDetails;


public interface GsmaTacDetailsRepository
        extends JpaRepository<GsmaTacDetails, Long>, JpaSpecificationExecutor<GsmaTacDetails> {

    public GsmaTacDetails getByTac(String tac);

}
