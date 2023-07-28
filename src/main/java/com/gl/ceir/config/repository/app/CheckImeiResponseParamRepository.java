package com.gl.ceir.config.repository.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.app.CheckImeiResponseParam;

public interface CheckImeiResponseParamRepository extends JpaRepository<CheckImeiResponseParam, Long>, JpaSpecificationExecutor<CheckImeiResponseParam> {


    public CheckImeiResponseParam getByTag(String tag);

    public CheckImeiResponseParam getById(Long id);

    public CheckImeiResponseParam getByTagAndTypeAndFeatureName(String tag, int type, String featureName);


}
