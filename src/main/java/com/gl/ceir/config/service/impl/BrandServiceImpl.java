package com.gl.ceir.config.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gl.ceir.config.exceptions.ResourceServicesException;
import com.gl.ceir.config.model.app.DevBrandName;
import com.gl.ceir.config.repository.app.BrandRepository;

@Service
public class BrandServiceImpl {

    @Autowired
    private BrandRepository brandRepository;

    private static final Logger logger = LogManager.getLogger(BrandServiceImpl.class);

    public List<DevBrandName> getAllBrands() {
        try {
            logger.info("Going to get All Brand List ");
            return brandRepository.findAll();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResourceServicesException(this.getClass().getName(), e.getMessage());
        }

//                
//                 public Sort sort(){
//                        return new Sort();
//                        }
    }
    //private final Path fileStorageLocation;

}
