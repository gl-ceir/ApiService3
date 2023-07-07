package com.gl.ceir.config.controller;

import com.gl.ceir.config.exceptions.MissingRequestParameterException;
import com.gl.ceir.config.exceptions.UnAuthorizationException;
import com.gl.ceir.config.exceptions.UnprocessableEntityException;
import com.gl.ceir.config.model.app.AppDeviceDetailsDb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gl.ceir.config.model.app.CheckImeiValuesEntity;
import com.gl.ceir.config.model.app.CheckImeiMess;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.DeviceidBaseUrlDb;
import com.gl.ceir.config.model.app.FeatureIpAccessList;
import com.gl.ceir.config.model.app.User;
import com.gl.ceir.config.model.app.UserFeatureIpAccessList;
import com.gl.ceir.config.service.impl.CheckImeiServiceImpl;
import com.gl.ceir.config.service.impl.LanguageServiceImpl;
import com.gl.ceir.config.model.constants.LanguageFeatureName;
import com.gl.ceir.config.repository.app.FeatureIpAccessListRepository;
import com.gl.ceir.config.repository.app.SystemConfigListRepository;
import com.gl.ceir.config.repository.app.SystemConfigurationDbRepository;
import com.gl.ceir.config.repository.app.UserFeatureIpAccessListRepository;
import com.gl.ceir.config.repository.app.UserRepository;

import io.swagger.annotations.ApiOperation;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class CheckImeiController {  //sachin

    private static final Logger logger = LogManager.getLogger(CheckImeiController.class);

    @Autowired
    CheckImeiServiceImpl checkImeiServiceImpl;

    @Autowired
    LanguageServiceImpl languageServiceImpl;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    SystemConfigListRepository systemConfigListRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SystemConfigurationDbRepository systemConfigurationDbRepository;

    @Autowired
    FeatureIpAccessListRepository featureIpAccessListRepository;

    @Autowired
    UserFeatureIpAccessListRepository userFeatureIpAccessListRepository;

//    @PostMapping(path = "cc/CheckImeI")
//    public MappingJacksonValue CheckImeiValues(@RequestBody CheckImeiValuesEntity checkImeiValuesEntity) {
//        String user_type = checkImeiValuesEntity.getUser_type().trim();
//        String feature = checkImeiValuesEntity.getFeature().trim().replaceAll(" ", "");
//        String imei = checkImeiValuesEntity.getImei();
//        Long imei_type = checkImeiValuesEntity.getImei_type();
//        logger.info("Feature   " + feature + user_type);
//        logger.info("UsrType   " + user_type);
//        logger.info("Imei_type (devIdType)   " + imei_type);
//        logger.info("Imei   " + imei);
//        CheckImeiMess cImsg = new CheckImeiMess();
//        MappingJacksonValue mapping = null;
//        String rulePass = checkImeiServiceImpl.getResult(user_type, feature, imei, imei_type);
//        logger.info("rulePass Value =" + rulePass);
//        if (rulePass.equalsIgnoreCase("true")) {
//            cImsg.setErrorMessage("NA");
//            cImsg.setStatus("Pass");
//            cImsg.setDeviceId(imei.substring(0, 8));
//        } else {
//            cImsg.setErrorMessage(rulePass);
//            cImsg.setStatus("Fail");
//            cImsg.setDeviceId(imei.substring(0, 8));
//        }
//        mapping = new MappingJacksonValue(cImsg);
//        return mapping;
//    }
    @ApiOperation(value = "Pre Init Api to get  Server", response = DeviceidBaseUrlDb.class)
    @RequestMapping(path = "service/preInit", method = RequestMethod.GET)
    public MappingJacksonValue getPreInit(@RequestParam("deviceId") String deviceId) {
        MappingJacksonValue mapping = new MappingJacksonValue(checkImeiServiceImpl.getPreinitApi(deviceId));
        logger.info("Response of View =" + mapping);
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
    public ResponseEntity checkImeiDevice(@RequestBody CheckImeiRequest checkImeiRequest) {
        String userIp = request.getHeader("HTTP_CLIENT_IP") == null
                ? (request.getHeader("X-FORWARDED-FOR") == null ? request.getRemoteAddr()
                : request.getHeader("X-FORWARDED-FOR"))
                : request.getHeader("HTTP_CLIENT_IP");

        logger.info("HTTP_CLIENT_IP " + request.getHeader("HTTP_CLIENT_IP"));
        logger.info("X-FORWARDED-FOR " + request.getHeader("X-FORWARDED-FOR"));
        logger.info("getRemoteAddr " + request.getRemoteAddr());
        logger.info("getServletPath " + request.getServletPath());
        logger.info("getRemoteAddr " + request.getRemoteAddr());
        logger.info("getAuthType " + request.getAuthType());
        logger.info("getLocalAddr " + request.getLocalAddr());
        logger.info("getRemoteHost " + request.getRemoteHost());
        logger.info("userIp " + userIp);
        logger.info("ClientIP " + request.getHeader("Client-IP"));
        logger.info("user-agent " + request.getHeader("user-agent"));
        checkImeiRequest.setHeader_browser(request.getHeader("user-agent"));
        checkImeiRequest.setHeader_public_ip(userIp);
        errorValidationChecker(checkImeiRequest);
        authorizationChecker(checkImeiRequest);
        logger.info("Going for values ");
        var value = checkImeiServiceImpl.getImeiDetailsDevices(checkImeiRequest);
        logger.info("Request = " + checkImeiRequest.toString() + " ; Response =" + value.toString());
        return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY).body(new MappingJacksonValue(value));
    }

    void errorValidationChecker(CheckImeiRequest checkImeiRequest) {
        logger.info(checkImeiRequest.toString());
        if (checkImeiRequest.getImei() == null || checkImeiRequest.getChannel() == null) {
            logger.info("Null Values " + checkImeiRequest.getImei());
            throw new MissingRequestParameterException(this.getClass().getName(), "parameter missing");
        }
        if (checkImeiRequest.getImei().isBlank()
                || (checkImeiRequest.getChannel().isBlank())
                || (!Arrays.asList("web", "ussd", "sms", "phone", "app").contains(checkImeiRequest.getChannel().toLowerCase()))
                || (checkImeiRequest.getImsi() != null && (checkImeiRequest.getImsi().length() != 15 || !(checkImeiRequest.getImsi().matches("[0-9]+"))))
                || (checkImeiRequest.getMsisdn() != null && (checkImeiRequest.getMsisdn().trim().length() > 20 || !(checkImeiRequest.getMsisdn().matches("[0-9 ]+"))))
                || (checkImeiRequest.getLanguage() != null && checkImeiRequest.getLanguage().trim().length() > 2)
                || (checkImeiRequest.getOperator() != null && checkImeiRequest.getOperator().trim().length() > 10)
                || (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getImsi() == null || checkImeiRequest.getOperator() == null || checkImeiRequest.getOperator().isBlank() || checkImeiRequest.getMsisdn().isBlank() || checkImeiRequest.getImsi().length() != 15 || !checkImeiRequest.getImsi().matches("[0-9]+")))
                || (checkImeiRequest.getChannel().equalsIgnoreCase("sms") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getMsisdn().isBlank() || checkImeiRequest.getOperator() == null || checkImeiRequest.getOperator().isBlank()))) {
            logger.info("Not allowed " + checkImeiRequest.getChannel());
            throw new UnprocessableEntityException(this.getClass().getName(), "provide mandatory field");
        }
    }

    void errorValidationChecker(AppDeviceDetailsDb appDeviceDetailsDb) {
        logger.info(appDeviceDetailsDb.toString());
        if (appDeviceDetailsDb.getDeviceDetails() == null || appDeviceDetailsDb.getDeviceId() == null || appDeviceDetailsDb.getLanguageType() == null || appDeviceDetailsDb.getOsType() == null) {
            throw new MissingRequestParameterException(this.getClass().getName(), "parameter missing");
        }
        if (appDeviceDetailsDb.getDeviceId().isBlank()
                || appDeviceDetailsDb.getLanguageType().trim().length() < 2
                || appDeviceDetailsDb.getOsType().isBlank()
                || appDeviceDetailsDb.getDeviceId().trim().length() > 50) {
            throw new UnprocessableEntityException(this.getClass().getName(), "provide specified field value");
        }
    }

    private void authorizationChecker(CheckImeiRequest checkImeiRequest) {
        if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || (checkImeiRequest.getChannel().equalsIgnoreCase("sms"))) {
            if (!Optional.ofNullable(request.getHeader("Authorization")).isPresent() || !request.getHeader("Authorization").startsWith("Basic ")) {
                logger.info("Rejected Due to  Authorization  Not Present");
                throw new UnAuthorizationException(this.getClass().getName(), "access denied");
            }
            logger.info("Basic Authorization present " + request.getHeader("Authorization").substring(6));
            try {
            var systemConfig = systemConfigListRepository.findByTagAndInterp("OPERATORS", checkImeiRequest.getOperator().toUpperCase());
            if (systemConfig == null) {
                logger.info("Operator Not allowed ");
                throw new UnprocessableEntityException(this.getClass().getName(), "provide correct operator");
            }
                logger.info("Found operator with  value " + systemConfig.getValue());
                var decodedString = new String(Base64.getDecoder().decode(request.getHeader("Authorization").substring(6)));
                logger.info("user:" + decodedString.split(":")[0] + "pass:" + decodedString.split(":")[1]);

                User userValue = userRepository
                        .getByUsernameAndPasswordAndParentId(decodedString.split(":")[0], decodedString.split(":")[1], systemConfig.getValue());
                if (userValue == null || !userValue.getUsername().equals(decodedString.split(":")[0]) || !userValue.getPassword().equals(decodedString.split(":")[1])) {
                    logger.info("username password not match");
                    throw new UnAuthorizationException(this.getClass().getName(), "access denied");
                }
                var checkimeiFeatureType = systemConfigurationDbRepository.getByTag("CHECK_IMEI_FEATURE_ID").getValue();
                FeatureIpAccessList featureIpAccessList = featureIpAccessListRepository.getByFeatureId(checkimeiFeatureType);
                logger.info(" data in featureIpAccessList  " + featureIpAccessList);
                if (featureIpAccessList == null) {
                    throw new UnAuthorizationException(this.getClass().getName(), "access denied");
                }
                if (featureIpAccessList.getTypeOfCheck() == 1) {
                    if (!featureIpAccessList.getIpAddress().contains(checkImeiRequest.getPublic_ip())) {
                        logger.info("Type Check 1 But Ip not allowed ");
                        throw new UnAuthorizationException(this.getClass().getName(), "access denied");
                    }
                } else {
                    logger.info("Type Check 2 with featureid  " + featureIpAccessList.getFeatureIpListId() + " And User id " + userValue.getId());
                    UserFeatureIpAccessList userFeatureIpAccessList = userFeatureIpAccessListRepository.getByFeatureIpListIdAndUserId(featureIpAccessList.getFeatureIpListId(), userValue.getId());
                    logger.info("Response from  UserFeatureIpAccessList " + userFeatureIpAccessList);
                    if (userFeatureIpAccessList == null || !(userFeatureIpAccessList.getIpAddress().contains(checkImeiRequest.getPublic_ip()))) {
                        logger.info("Type Check 2 But Ip not allowed ");
                        throw new UnAuthorizationException(this.getClass().getName(), "access denied");
                    }

                }
                logger.info("Authentication Pass ");
            } catch (Exception e) {
                logger.info("Authentication fail" + e);
                throw new UnAuthorizationException(this.getClass().getName(), "access denied");
            }
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
