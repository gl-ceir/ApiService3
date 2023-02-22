package com.gl.ceir.config.controller;

import com.gl.ceir.config.model.AppDeviceDetailsDb;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gl.ceir.config.model.CheckImeiValuesEntity;

import com.gl.ceir.config.model.CheckImeiMess;
import com.gl.ceir.config.model.CheckImeiRequest;
import com.gl.ceir.config.model.CheckImeiResponse;
import com.gl.ceir.config.service.impl.CheckImeiServiceImpl;
import com.gl.ceir.config.service.impl.LanguageServiceImpl;
import com.gl.ceir.config.model.constants.LanguageFeatureName;

import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class CheckImeiController {  //sachin

    private static final Logger logger = Logger.getLogger(GsmaValueController.class);

    @Autowired
    CheckImeiServiceImpl checkImeiServiceImpl;

    @Autowired
    LanguageServiceImpl languageServiceImpl;

    @PostMapping(path = "cc/CheckImeI")
    public MappingJacksonValue CheckImeiValues(@RequestBody CheckImeiValuesEntity checkImeiValuesEntity) {
        String user_type = checkImeiValuesEntity.getUser_type().trim();
        String feature = checkImeiValuesEntity.getFeature().trim().replaceAll(" ", "");
        String imei = checkImeiValuesEntity.getImei();
        Long imei_type = checkImeiValuesEntity.getImei_type();
        logger.info("Feature   " + feature + user_type);
        logger.info("UsrType   " + user_type);
        logger.info("Imei_type (devIdType)   " + imei_type);
        logger.info("Imei   " + imei);
        CheckImeiMess cImsg = new CheckImeiMess();
        MappingJacksonValue mapping = null;
        String rulePass = checkImeiServiceImpl.getResult(user_type, feature, imei, imei_type);
        logger.info("rulePass Value =" + rulePass);
        if (rulePass.equalsIgnoreCase("true")) {
            cImsg.setErrorMessage("NA");
            cImsg.setStatus("Pass");
            cImsg.setDeviceId(imei.substring(0, 8));
        } else {
            cImsg.setErrorMessage(rulePass);
            cImsg.setStatus("Fail");
            cImsg.setDeviceId(imei.substring(0, 8));
        }
        mapping = new MappingJacksonValue(cImsg);
        return mapping;
    }

    @ApiOperation(value = "check Imei Api", response = CheckImeiResponse.class)
    @PostMapping("checkImeiApi")
    public MappingJacksonValue checkImeiDevice(@RequestBody CheckImeiRequest checkImeiRequest) {
        return new MappingJacksonValue(checkImeiServiceImpl.getImeiDetailsDevices(checkImeiRequest));
    }

    @ApiOperation(value = "Mobile Details", response = String.class)
    @PostMapping("MobileDeviceDetails/save")
    public MappingJacksonValue getMobileDeviceDetails(@RequestBody AppDeviceDetailsDb appDeviceDetailsDb) {         
        checkImeiServiceImpl.saveDeviceDetails(appDeviceDetailsDb);
        return new MappingJacksonValue(languageServiceImpl.getLanguageLabels(LanguageFeatureName.CHECKIMEI.name(), "english"));
    }

}
