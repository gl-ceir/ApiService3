package com.gl.ceir.config.controller;

import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.exceptions.MissingRequestParameterException;
import com.gl.ceir.config.exceptions.ResourceServicesException;
import com.gl.ceir.config.exceptions.UnprocessableEntityException;
import com.gl.ceir.config.model.app.AppDeviceDetailsDb;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gl.ceir.config.model.app.CheckImeiValuesEntity;
import com.gl.ceir.config.model.app.CheckImeiMess;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.service.impl.CheckImeiServiceImpl;
import com.gl.ceir.config.service.impl.LanguageServiceImpl;
import com.gl.ceir.config.model.constants.LanguageFeatureName;

import io.swagger.annotations.ApiOperation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class CheckImeiController {  //sachin

    private static final Logger logger = Logger.getLogger(CheckImeiController.class);

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

    @ApiOperation(value = "Mobile Details", response = String.class)
    @PostMapping("MobileDeviceDetails/save")
    public MappingJacksonValue getMobileDeviceDetails(@RequestBody AppDeviceDetailsDb appDeviceDetailsDb) {
        errorValidationChecker(appDeviceDetailsDb);
        logger.info("Request = " + appDeviceDetailsDb);
        checkImeiServiceImpl.saveDeviceDetails(appDeviceDetailsDb);
        logger.info("Going to fetch response according to  = " + appDeviceDetailsDb.getLanguageType());
        return new MappingJacksonValue(languageServiceImpl.getLanguageLabels(LanguageFeatureName.CHECKIMEI.name(), appDeviceDetailsDb.getLanguageType()));
    }

    @ApiOperation(value = "check Imei Api", response = CheckImeiResponse.class)
    @PostMapping("services/checkIMEI")
    public ResponseEntity<MappingJacksonValue> checkImeiDevice(@RequestBody CheckImeiRequest checkImeiRequest) {
        var value = checkImeiServiceImpl.getImeiDetailsDevices(checkImeiRequest);
        logger.info("Request = " + checkImeiRequest.toString() + " ; Response =" + value);
        return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY)
                .body(new MappingJacksonValue(value));
    }

     void errorValidationChecker(AppDeviceDetailsDb appDeviceDetailsDb) {
        logger.info(appDeviceDetailsDb.toString());
        logger.info("sdfsdf+" + appDeviceDetailsDb.getLanguageType());

        if ( appDeviceDetailsDb.getDeviceDetails() == null ||  appDeviceDetailsDb.getDeviceId() == null || appDeviceDetailsDb.getLanguageType() == null || appDeviceDetailsDb.getOsType() == null) {
            throw new MissingRequestParameterException(this.getClass().getName(), "parameter missing");
        }
        if (appDeviceDetailsDb.getDeviceId().isBlank() || appDeviceDetailsDb.getLanguageType().trim().length() < 2 || appDeviceDetailsDb.getOsType().isBlank()) {
            throw new UnprocessableEntityException(this.getClass().getName(), "provide mandatory field");
        }
    }

}

//    @ApiOperation(value = "check Imei Api v2", response = CheckImeiResponse.class)
//    @PostMapping("checkImeiApiV1")
//    public MappingJacksonValue checkImeiDeviceV1(@RequestBody CheckImeiRequest checkImeiRequest) {
//        var result = checkImeiServiceImpl.getImeiDetailsDevices(checkImeiRequest);
//        logger.info("result   " + result);
//        return new MappingJacksonValue(result);
// }
/*
            HttpHeaders responseHeaders = new HttpHeaders();

                return new ResponseEntity<String>(result, responseHeaders,  genericModel.getHttpStatus());

 */
