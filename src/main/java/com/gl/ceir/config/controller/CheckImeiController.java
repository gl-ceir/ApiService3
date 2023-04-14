package com.gl.ceir.config.controller;

import com.gl.ceir.config.exceptions.MissingRequestParameterException;
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
import java.util.Arrays;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;

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
        errorValidationChecker(checkImeiRequest);
        logger.info("Going for values ");
        var value = checkImeiServiceImpl.getImeiDetailsDevices(checkImeiRequest);
        logger.info("Request = " + checkImeiRequest.toString() + " ; Response =" + value);
        return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY)
                .body(new MappingJacksonValue(value));

    }

    void errorValidationChecker(CheckImeiRequest checkImeiRequest) {
        logger.info(checkImeiRequest.toString());
        // imei not present 
        if (checkImeiRequest.getImei() == null || checkImeiRequest.getChannel() == null) {
            logger.info("Null Values " + checkImeiRequest.getImei());
            throw new MissingRequestParameterException(this.getClass().getName(), "parameter missing");
        }
        //  "imei": "",
        if (checkImeiRequest.getImei().isBlank()
                || (checkImeiRequest.getChannel().isBlank())
                || (!Arrays.asList("web", "ussd", "sms", "phone", "app").contains(checkImeiRequest.getChannel().toLowerCase()))
                || (checkImeiRequest.getImsi() != null && (checkImeiRequest.getImsi().length() != 15 || !(checkImeiRequest.getImsi().matches("[0-9]+"))))
                || (checkImeiRequest.getMsisdn() != null && (checkImeiRequest.getMsisdn().trim().length() > 20 || !(checkImeiRequest.getMsisdn().matches("[0-9 ]+"))))
                || (checkImeiRequest.getLanguage() != null && checkImeiRequest.getLanguage().trim().length() > 2)
                || (checkImeiRequest.getOperator() != null && checkImeiRequest.getOperator().trim().length() > 10)
                || (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getImsi() == null || checkImeiRequest.getMsisdn().isBlank()  || checkImeiRequest.getImsi().length() != 15 || !checkImeiRequest.getImsi().matches("[0-9]+") ))
                || (checkImeiRequest.getChannel().equalsIgnoreCase("sms") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getMsisdn().isBlank()))
                ) {
            logger.info("Not allowed " + checkImeiRequest.getChannel());
            throw new UnprocessableEntityException(this.getClass().getName(), "provide mandatory field");
        }
    }

    void errorValidationChecker(AppDeviceDetailsDb appDeviceDetailsDb) {
        logger.info(appDeviceDetailsDb.toString());
        if (appDeviceDetailsDb.getDeviceDetails() == null || appDeviceDetailsDb.getDeviceId() == null || appDeviceDetailsDb.getLanguageType() == null || appDeviceDetailsDb.getOsType() == null) {
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

   // imei not present 
        if (checkImeiRequest.getImei() == null || checkImeiRequest.getChannel() == null) {
            logger.info("NullVals " + checkImeiRequest.getImei());
            throw new MissingRequestParameterException(this.getClass().getName(), "parameter missing");
        }

        //  "imei": "",
        if (checkImeiRequest.getImei().isBlank() || checkImeiRequest.getChannel().isBlank()) {
            logger.info("BlankVals " + checkImeiRequest.getImei());
            throw new UnprocessableEntityException(this.getClass().getName(), "provide standardised value");
        }

        if (!Arrays.asList("web", "ussd", "sms", "phone", "app").contains(checkImeiRequest.getChannel().toLowerCase())) {
            logger.info("Not contains " + checkImeiRequest.getChannel());
            throw new UnprocessableEntityException(this.getClass().getName(), "provide standardised value");
        }

        if (checkImeiRequest.getImsi() != null && checkImeiRequest.getImsi().length() != 15) {
            logger.info("imsi not 15 " + checkImeiRequest.getImsi());
            throw new UnprocessableEntityException(this.getClass().getName(), "provide standardised value");
        }

        if ((checkImeiRequest.getChannel().equalsIgnoreCase("ussd") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getImsi() == null))
                || (checkImeiRequest.getChannel().equalsIgnoreCase("sms") && checkImeiRequest.getMsisdn() == null)) {
            throw new UnprocessableEntityException(this.getClass().getName(), "provide mandatory field");
        }
        if (checkImeiRequest.getImei().trim().length() < 1) {
            throw new MissingRequestParameterException(this.getClass().getName(), "parameter missing");
        }




 */
