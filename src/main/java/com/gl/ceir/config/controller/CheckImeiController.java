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
import com.gl.ceir.config.repository.app.AppDeviceDetailsRepository;
import com.gl.ceir.config.repository.app.CheckImeiResponseParamRepository;
import com.gl.ceir.config.repository.app.FeatureIpAccessListRepository;
import com.gl.ceir.config.repository.app.SystemConfigListRepository;
import com.gl.ceir.config.repository.app.SystemConfigurationDbRepository;
import com.gl.ceir.config.repository.app.UserFeatureIpAccessListRepository;
import com.gl.ceir.config.repository.app.UserRepository;

//import io.swagger.annotations.ApiOperation;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class CheckImeiController {  //sachin

    private static final Logger logger = LogManager.getLogger(CheckImeiController.class);

    @Value("${local-ip}")
    public String localIp;
    @Value("${authFail}")
    private String authFail;
    @Value("${authUserIpNotMatch}")
    private String authUserIpNotMatch;
    @Value("${authFeatureIpNotMatch}")
    private String authFeatureIpNotMatch;
    @Value("${authFeatureIpNotPresent}")
    private String authFeatureIpNotPresent;
    @Value("${authUserPassNotMatch}")
    private String authUserPassNotMatch;
    @Value("${authOperatorNotPresent}")
    private String authOperatorNotPresent;
    @Value("${authNotPresent}")
    private String authNotPresent;
    @Value("${requiredValueNotPresent}")
    private String requiredValueNotPresent;
    @Value("${mandatoryParameterMissing}")
    private String mandatoryParameterMissing;
    @Value("${nullPointerException}")
    private String nullPointerException;
    @Value("${sqlException}")
    private String sQLException;
    @Value("${someWentWrongException}")
    private String someWentWrongException;

    @Value("#{'${languageType}'.split(',')}")
    public List<String> languageType;

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
    SystemConfigurationDbRepository systemConfigurationDbRepositry;

    @Autowired
    CheckImeiResponseParamRepository checkImeiResponseParamRepository;

    @Autowired
    FeatureIpAccessListRepository featureIpAccessListRepository;

    @Autowired
    UserFeatureIpAccessListRepository userFeatureIpAccessListRepository;

    @Autowired
    AppDeviceDetailsRepository appDeviceDetailsRepository;

    //@ApiOperation(value = "Pre Init Api to get  Server", response = DeviceidBaseUrlDb.class)
    @CrossOrigin(origins = "", allowedHeaders = "")
    @RequestMapping(path = "services/mobile_api/preInit", method = RequestMethod.GET)
    public MappingJacksonValue getPreInit(@RequestParam("deviceId") String deviceId) {
        String host = request.getHeader("Host");
        System.out.println("Host Name " + host);
        logger.info("Host Name::: " + host);
        MappingJacksonValue mapping = new MappingJacksonValue(checkImeiServiceImpl.getPreinitApi(deviceId));
        logger.info("Response of View =" + mapping);
        return mapping;
    }

    //@ApiOperation(value = "Mobile Details", response = String.class)
    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/mobile_api/mobileDeviceDetails/save")
    public MappingJacksonValue getMobileDeviceDetails(@RequestBody AppDeviceDetailsDb appDeviceDetailsDb) {
        errorValidationChecker(appDeviceDetailsDb);
        logger.info("Request = " + appDeviceDetailsDb);
        checkImeiServiceImpl.saveDeviceDetails(appDeviceDetailsDb);
        logger.info("Going to fetch response according to  = " + appDeviceDetailsDb.getLanguageType());
        return new MappingJacksonValue(languageServiceImpl.getLanguageLabels(LanguageFeatureName.CHECKIMEI.name(), appDeviceDetailsDb.getLanguageType()));
    }

    void errorValidationChecker(AppDeviceDetailsDb appDeviceDetailsDb) {
        logger.info(appDeviceDetailsDb.toString());
        if (appDeviceDetailsDb.getDeviceDetails() == null || appDeviceDetailsDb.getDeviceId() == null || appDeviceDetailsDb.getLanguageType() == null || appDeviceDetailsDb.getOsType() == null) {
            throw new MissingRequestParameterException("en", "parameter missing");
        }
        if (appDeviceDetailsDb.getDeviceId().isBlank()
                || appDeviceDetailsDb.getLanguageType().trim().length() < 2
                || appDeviceDetailsDb.getOsType().isBlank()
                || appDeviceDetailsDb.getDeviceId().trim().length() > 50) {
            throw new UnprocessableEntityException("en", "provide specified field value");
        }
    }

    /*  *******************************  */
    //@ApiOperation(value = "check Imei Api", response = CheckImeiResponse.class)
    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/checkIMEI")
    public ResponseEntity checkImeiDevice(@RequestBody CheckImeiRequest checkImeiRequest) {
        var startTime = System.currentTimeMillis();  // this can be stored in setRequestProcessStatus
        String userIp = request.getHeader("HTTP_CLIENT_IP") == null
                ? (request.getHeader("X-FORWARDED-FOR") == null ? request.getRemoteAddr()
                : request.getHeader("X-FORWARDED-FOR"))
                : request.getHeader("HTTP_CLIENT_IP");
//        try {
//            logger.debug("user-agent " + request.getHeader("user-agent") + ";ClientIP " + request.getHeader("Client-IP") + ";userIp " + userIp + ";getRemoteHost " + request.getRemoteHost() + ";getLocalAddr " + request.getLocalAddr() + ";getAuthType " + request.getAuthType() + ";getRemoteAddr " + request.getRemoteAddr() + ";getServletPath " + request.getServletPath() + ";getRemoteAddr " + request.getRemoteAddr() + ";X-FORWARDED-FOR " + request.getHeader("X-FORWARDED-FOR") + "; HTTP_CLIENT_IP " + request.getHeader("HTTP_CLIENT_IP"));
//        } catch (Exception e) {
//            logger.warn("Getting Ips for Testing purpose", e);
//        }
      //  logger.info("!!!!!!!! Request = " + checkImeiRequest.toString());
        checkImeiRequest.setHeader_browser(request.getHeader("user-agent"));
        checkImeiRequest.setHeader_public_ip(userIp);
        var language = checkImeiRequest.getLanguage() == null ? "en" : checkImeiRequest.getLanguage().equalsIgnoreCase("kh") ? "kh" : "en";
        checkImeiRequest.setLanguage(language);    // needs refactoring
        logger.info(checkImeiRequest.toString());
        errorValidationChecker(checkImeiRequest, startTime);
        authorizationChecker(checkImeiRequest, startTime);
        logger.debug("Going for values ");
        var value = checkImeiServiceImpl.getImeiDetailsDevices(checkImeiRequest, startTime);
        logger.info("   Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + " ########## Response =" + value.toString());
        return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY).body(new MappingJacksonValue(value));
    }

    void errorValidationChecker(CheckImeiRequest checkImeiRequest, long startTime) {
        if (checkImeiRequest.getImei() == null || checkImeiRequest.getChannel() == null) {
            logger.debug("Null Values " + checkImeiRequest.getImei());
            checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, mandatoryParameterMissing);
            throw new MissingRequestParameterException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
        if (checkImeiRequest.getImei().isBlank()
                || (checkImeiRequest.getChannel().isBlank())
                || (!Arrays.asList("web", "ussd", "sms", "phone", "app").contains(checkImeiRequest.getChannel().toLowerCase()))
                || (checkImeiRequest.getImsi() != null && (checkImeiRequest.getImsi().length() != 15 || !(checkImeiRequest.getImsi().matches("[0-9]+"))))
                || (checkImeiRequest.getMsisdn() != null && (checkImeiRequest.getMsisdn().trim().length() > 20 || !(checkImeiRequest.getMsisdn().matches("[0-9 ]+"))))
                || (checkImeiRequest.getLanguage() != null && checkImeiRequest.getLanguage().trim().length() > 2)
                || (checkImeiRequest.getOperator() != null && checkImeiRequest.getOperator().trim().length() > 20)
                || (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getOperator() == null || checkImeiRequest.getOperator().isBlank() || checkImeiRequest.getMsisdn().isBlank()))
                || (checkImeiRequest.getChannel().equalsIgnoreCase("sms") && (checkImeiRequest.getMsisdn() == null || checkImeiRequest.getMsisdn().isBlank() || checkImeiRequest.getOperator() == null || checkImeiRequest.getOperator().isBlank()))) {
            logger.info("Not allowed " + checkImeiRequest.getChannel());
            checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, requiredValueNotPresent);
            throw new UnprocessableEntityException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    private void authorizationChecker(CheckImeiRequest checkImeiRequest, long startTime) {
        if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || (checkImeiRequest.getChannel().equalsIgnoreCase("sms"))) {
            if (!Optional.ofNullable(request.getHeader("Authorization")).isPresent() || !request.getHeader("Authorization").startsWith("Basic ")) {
                logger.info("Rejected Due to  Authorization  Not Present" + request.getHeader("Authorization"));
                checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, authNotPresent);
                throw new UnAuthorizationException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
            }
            logger.info("Basic Authorization present " + request.getHeader("Authorization").substring(6));
            try {
                var systemConfig = systemConfigListRepository.findByTagAndInterp("OPERATORS", checkImeiRequest.getOperator().toUpperCase());
                if (systemConfig == null) {
                    logger.info("Operator Not allowed ");
                    checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, authOperatorNotPresent);
                    throw new UnprocessableEntityException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                }
                logger.info("Found operator with  value " + systemConfig.getValue());
                var decodedString = new String(Base64.getDecoder().decode(request.getHeader("Authorization").substring(6)));
                logger.info("user:" + decodedString.split(":")[0] + "pass:" + decodedString.split(":")[1]);
                User userValue = userRepository
                        .getByUsernameAndPasswordAndParentId(decodedString.split(":")[0], decodedString.split(":")[1], systemConfig.getValue());
                if (userValue == null || !userValue.getUsername().equals(decodedString.split(":")[0]) || !userValue.getPassword().equals(decodedString.split(":")[1])) {
                    logger.info("username password not match");
                    checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, authUserPassNotMatch);
                    throw new UnAuthorizationException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                }

                if (systemConfigurationDbRepositry.getByTag("CHECK_IMEI_AUTH_WITH_IP").getValue().equalsIgnoreCase("true")) {
                    var checkimeiFeatureType = systemConfigurationDbRepositry.getByTag("CHECK_IMEI_FEATURE_ID").getValue();
                    FeatureIpAccessList featureIpAccessList = featureIpAccessListRepository.getByFeatureId(checkimeiFeatureType);
                    logger.info(" data in featureIpAccessList  " + featureIpAccessList);
                    if (featureIpAccessList == null) {
                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, authFeatureIpNotPresent);
                        throw new UnAuthorizationException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                    }
                    if (featureIpAccessList.getTypeOfCheck() == 1) {
                        if (!featureIpAccessList.getIpAddress().contains(checkImeiRequest.getHeader_public_ip())) {
                            logger.info("Type Check 1 But Ip not allowed ");
                            checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, authFeatureIpNotMatch);
                            throw new UnAuthorizationException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                        }
                    } else {
                        logger.info("Type Check 2 with featureid  " + featureIpAccessList.getFeatureIpListId() + " And User id " + userValue.getId());
                        UserFeatureIpAccessList userFeatureIpAccessList = userFeatureIpAccessListRepository.getByFeatureIpListIdAndUserId(featureIpAccessList.getFeatureIpListId(), userValue.getId());
                        logger.info("Response from  UserFeatureIpAccessList " + userFeatureIpAccessList);
                        if (userFeatureIpAccessList == null || !(userFeatureIpAccessList.getIpAddress().contains(checkImeiRequest.getHeader_public_ip()))) {
                            logger.info("Type Check 2 But Ip not allowed ");
                            checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, authUserIpNotMatch);
                            throw new UnAuthorizationException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                        }
                    }
                }
                logger.debug("Authentication Pass ");
            } catch (NullPointerException | UnsupportedOperationException e) {
                logger.warn("Authentication fail" + e);
                throw new UnAuthorizationException(checkImeiRequest.getLanguage(), checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
            }
        }
    }

}

//                logger.info("Authentication FAIL++++++++++++++++++ ");
//                if (e instanceof NullPointerException) {
//                    logger.info("Authentication FAIL***************** ");
//
//                    checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, nullPointerException);
//                } else if (e instanceof SQLException) {
//                    logger.info("Authentication FAIL&&&&&&&&&&&&&&&&&&&&&&& ");
//
//                    checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, sQLException);
//                } else {
//                    logger.info("Authentication FAIL_______----------------------- ");
//
//                    checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, someWentWrongException);
//                }
//    //@ApiOperation(value = "check Imei Api v2", response = CheckImeiResponse.class)
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


 */
