package com.gl.ceir.config.controller;

import com.gl.ceir.config.exceptions.MissingRequestParameterException;
import com.gl.ceir.config.exceptions.ServiceUnavailableException;
import com.gl.ceir.config.exceptions.UnAuthorizationException;
import com.gl.ceir.config.exceptions.UnprocessableEntityException;
import com.gl.ceir.config.model.app.*;
import com.gl.ceir.config.model.constants.LanguageFeatureName;
import com.gl.ceir.config.repository.app.*;
import com.gl.ceir.config.service.impl.*;
import com.gl.ceir.config.service.userlogic.UserFactory;
import com.gl.ceir.config.validate.CheckImeiValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

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
    CheckImeiValidator checkImeiValidator;

    @Autowired
    CheckImeiOtherApiImpl checkImeiOtherApiImpl;

    @Autowired
    CheckImeiServiceImpl checkImeiServiceImpl;

    @Autowired
    CheckImeiServiceImpl_V2 checkImeiServiceImplV2;

    @Autowired
    LanguageServiceImpl languageServiceImpl;

    @Autowired
    private HttpServletRequest request;


    @Autowired
    SystemParamServiceImpl sysPrmSrvcImpl;


    @Autowired
    FeatureMenuServiceImpl featureMenuServiceImpl;


    //@ApiOperation(value = "Pre Init Api to get  Server", response = DeviceidBaseUrlDb.class)
    @CrossOrigin(origins = "", allowedHeaders = "")
    @RequestMapping(path = "services/mobile_api/preInit", method = RequestMethod.GET)
    public MappingJacksonValue getPreInit(@RequestParam("deviceId") String deviceId) {
        String host = request.getHeader("Host");
        logger.info("Host Name::: " + host);
        logger.info("MENU LIST ::: " + featureMenuServiceImpl.getAll());
        MappingJacksonValue mapping = new MappingJacksonValue(checkImeiOtherApiImpl.getPreinitApi(deviceId));
        logger.info("Response of View =" + mapping);
        return mapping;
    }

    //@ApiOperation(value = "Mobile Details", response = String.class)
    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/mobile_api/mobileDeviceDetails/save")
    public MappingJacksonValue getMobileDeviceDetails(@RequestBody AppDeviceDetailsDb appDeviceDetailsDb) {
        checkImeiValidator.errorValidationChecker(appDeviceDetailsDb);
        logger.info("Request = " + appDeviceDetailsDb);
        checkImeiOtherApiImpl.saveDeviceDetails(appDeviceDetailsDb);
        logger.info("Going to fetch response according to  = " + appDeviceDetailsDb.getLanguageType());
        return new MappingJacksonValue(languageServiceImpl.getLanguageLabels(LanguageFeatureName.CHECKIMEI.getName(), appDeviceDetailsDb.getLanguageType()));
    }


    /*  *******************************  */
    //@ApiOperation(value = "check Imei Api", response = CheckImeiResponse.class)
    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/checkIMEI/v1")
    public ResponseEntity checkImeiDevice(@RequestBody CheckImeiRequest checkImeiRequest) {
        return startCheckImei(checkImeiRequest, "v1");
   }

    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/checkIMEI")
    public ResponseEntity checkImeiDeviceV2(@RequestBody CheckImeiRequest checkImeiRequest) {
        return startCheckImei(checkImeiRequest, "v2");
    }


    public ResponseEntity startCheckImei(CheckImeiRequest checkImeiRequest, String version) {
        var startTime = System.currentTimeMillis();
        var defaultLang = sysPrmSrvcImpl.getValueByTag("systemDefaultLanguage");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Map<String, String> headers = Collections.list(httpRequest.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, httpRequest::getHeader));
        logger.info("Headers->  {}", headers);
        checkImeiRequest.setLanguage(checkImeiRequest.getLanguage() == null ? defaultLang : checkImeiRequest.getLanguage().equalsIgnoreCase("kh") ? "kh" : defaultLang);    // needs refactoring
        checkImeiValidator.errorValidationChecker(checkImeiRequest, startTime);
        checkImeiValidator.authorizationChecker(checkImeiRequest, startTime);

        var value = version.equals("v2") ?
                checkImeiServiceImplV2.getImeiDetailsDevicesNew(checkImeiRequest, startTime) :
                checkImeiServiceImpl.getImeiDetailsDevicesNew(checkImeiRequest, startTime);
        logger.info("   Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + " ########## Response =" + value.toString());
        return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY).body(new MappingJacksonValue(value));
    }


}
