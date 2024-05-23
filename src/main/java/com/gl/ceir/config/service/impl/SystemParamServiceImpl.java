package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.exceptions.ResourceServicesException;
import com.gl.ceir.config.model.app.SystemConfigurationDb;
import com.gl.ceir.config.repository.app.RulesRepository;
import com.gl.ceir.config.repository.app.SystemConfigurationDbRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemParamServiceImpl {

    private static final Logger log = LogManager.getLogger(SystemParamServiceImpl.class);

    @Autowired
    SystemConfigurationDbRepository systemConfigurationDbRepositry;

    public String getValueByTag(String tag) {
        try {
            var value = systemConfigurationDbRepositry.getByTag(tag).getValue();
            if (value == null || value.isBlank()) {
                log.warn("No value found for tag " + tag + "  ");
                return null;
            }
            return value;
        } catch (Exception e) {
            log.warn("No value found for tag " + tag + "# Error : " + e.toString());
            return null;
        }
    }
}