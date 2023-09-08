package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.configuration.PropertiesReader;
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

    @Autowired
    private PropertiesReader propertiesReader;
    private static final Logger logger = LogManager.getLogger(BrandServiceImpl.class);

    public List<DevBrandName> getAllBrands() {
        try {
            List<DevBrandName> list1 = null;

            logger.info("Going for top 5 brands");
            List<String> topBrands = propertiesReader.getTop5Brands();
            logger.info("B Rands for top 5 brands" + topBrands.size());
            if (topBrands.size() < 5) {
                    logger.error("Brands size is less than 5, Please provide  Top 5 Brand Name in properties via  Top5Brands  :: size " + topBrands.size() + "[]");
            }
            //   try {
            list1 = brandRepository.getBrandNameWithTop5New(topBrands.get(0), topBrands.get(1), topBrands.get(2), topBrands.get(3), topBrands.get(4));
            logger.info("result 2:: " + list1.size() + "[]" + list1.get(0) + "[]" + list1.get(1) + "[]" + list1.get(4));
//            } catch (Exception e) {
//                logger.error("Error in 2 " + e.getLocalizedMessage() + "+++" + e.getMessage());
//            }

            return list1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResourceServicesException(this.getClass().getName(), e.getMessage());
        }

    }
    //private final Path fileStorageLocation;

}

//            logger.info("Going to get All Brand List ");
//            try {
//                logger.info("Going to get via 1 ");
////                List<DevBrandName> list1 = brandRepository.getBrandNameWithTop5("OPPO", "Apple", "Nokia", "vivo", "Samsung");
////                logger.info("result 1 :: " + list1.size() + "[]" + list1.get(0) + "[]" + list1.get(1) + "[]" + list1.get(4));
//            } catch (Exception e) {
//                logger.error("Error in 1 " + e.getLocalizedMessage() + "+++" + e.getMessage());
//            }
//
//                 public Sort sort(){
//                        return new Sort();
//                        }
                // list1 = brandRepository.getBrandNameWithTop5New1();
