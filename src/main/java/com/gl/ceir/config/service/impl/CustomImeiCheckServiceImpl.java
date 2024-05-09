package com.gl.ceir.config.service.impl;

import com.gl.RuleEngineAdaptor;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.CustomImeiCheckResponse;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.model.constants.CustomCheckImeiRequest;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.app.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Write a code :
 * get all values at the starting of api. like language sys_param checkImeiResponseParam etc.
 * create a function to check in database , if value of that tag is true:
 *  Call all values from db again. and once values are obtained. change sysParam_tag ->false
 * If  change sysParam_tag ->false. not to get values
 *
 * */

@Service
public class CustomImeiCheckServiceImpl {

    private static final Logger logger = LogManager.getLogger(CustomImeiCheckServiceImpl.class);

    @Value("${nullPointerException}")
    private String nullPointerException;

    @Value("${sqlException}")
    private String sQLException;

    @Value("${ruleResponseError}")
    private String ruleResponseError;

    @Value("${someWentWrongException}")
    private String someWentWrongException;

    @Value("#{'${checkImeiRemarkRules}'.split(',')}")
    public List<String> remarkRules;

    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    CheckImeiResponseParamRepository chkImeiRespPrmRepo;

    @Autowired
    GsmaTacDetailsRepository gsmaTacDetailsRepository;

    @Autowired
    CheckImeiRequestRepository checkImeiRequestRepository;

    @Autowired
    GdceCheckImeiReqRepository gdceCheckImeiReqRepository;

    @Autowired
    CheckImeiServiceSendSMS checkImeiServiceSendSMS;

    @Autowired
    NationalWhitelistRepository nationalWhitelistRepository;

    @Autowired
    DbRepository dbRepository;

    @Autowired
    private HttpServletRequest request;


    public List<CustomImeiCheckResponse> startService(List<CustomCheckImeiRequest> custChckImeiReq) {
        var conn = dbRepository.getConnection();
        String userIp = request.getHeader("HTTP_CLIENT_IP") == null ? (request.getHeader("X-FORWARDED-FOR") == null ? request.getRemoteAddr() : request.getHeader("X-FORWARDED-FOR")) : request.getHeader("HTTP_CLIENT_IP");
        String userAgent = request.getHeader("user-agent");
        var startTime = System.currentTimeMillis();
        List<CustomImeiCheckResponse> imeiResponse = new LinkedList<>();
        for (CustomCheckImeiRequest cusReq : custChckImeiReq) {
            CheckImeiRequest checkImeiRequest = new CheckImeiRequest();
            var deviceInfo = Map.of("appdbName", "app", "auddbName", "aud", "repdbName", "rep", "edrappdbName", "edrapp",
                    "imei", cusReq.getImei(), "feature", "CustomCheckImei");
            LinkedHashMap<String, Boolean> rules = RuleEngineAdaptor.startAdaptor(conn, deviceInfo);
            logger.info("Rules Return " + rules);
            var response = rules.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .reduce((first, second) -> second)
                    .orElse(null);
            logger.info("Finale " + response);
            if (response) {
                var tacDetail = gsmaTacDetailsRepository.getBydeviceId(cusReq.getImei().substring(0, 8));
                logger.info("tacDetail" + tacDetail);
                if (tacDetail == null) {
                    imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", "Device is not-compliant", "", "", ""));
                    checkImeiRequest.setRequestProcessStatus("Fail");
                    checkImeiRequest.setComplianceStatus("Device is not-compliant");
                } else {
                    imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "200", "Device is Compliant", tacDetail.getDevice_type(), tacDetail.getBrand_name(), tacDetail.getModel_name()));
                    checkImeiRequest.setRequestProcessStatus("Success");
                    checkImeiRequest.setComplianceStatus("Device is Compliant");
                }
            } else {
                imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", "Device is not-compliant", "", "", ""));
                checkImeiRequest.setRequestProcessStatus("Fail");
                checkImeiRequest.setComplianceStatus("Device is not-compliant");

            }
            checkImeiRequest.setImei(cusReq.getImei());
            checkImeiRequest.setChannel("API");
            checkImeiRequest.setHeader_browser(userAgent);
            checkImeiRequest.setHeader_public_ip(userIp);
            saveCheckImeiRequest(checkImeiRequest, startTime);
        }
        return imeiResponse;
    }

    public CheckImeiRequest saveCheckImeiRequest(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            checkImeiRequest.setCheckProcessTime(String.valueOf(System.currentTimeMillis() - startTime));
            var response = checkImeiRequestRepository.save(checkImeiRequest);
            return response;
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1110.getName(), 0);
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    public String globalErrorMsgs(String language) {
        return chkImeiRespPrmRepo.getByTagAndLanguage("CheckImeiErrorMessage", language).getValue();
    }

}


// public CheckImeiResponse getImeiDetailsDevicesNew(CheckImeiRequest checkImeiRequest, long startTime) {
//        try {
//
//            var rules = getResponseStatusViaRuleEngine(checkImeiRequest);
//            var responseTag = "CheckImeiResponse_" + checkImeiRequest.getComplianceValue();  // optimise
//            logger.info("Response Tag :: " + responseTag);
//            var result = getResult(checkImeiRequest, rules, responseTag);
//            var response = saveCheckImeiRequest(checkImeiRequest, startTime);
//            checkImeiServiceSendSMS.sendSMSforUSSD_SMS(checkImeiRequest, responseTag, response);
//            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()), StatusMessage.FOUND.getName(), checkImeiRequest.getLanguage(), result);
//        } catch (Exception e) {
//            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CustomImeiCheckServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
//            logger.error("Failed at " + e.getLocalizedMessage() + " ----- " + e.toString() + " ::::: " + e.getMessage() + " $$$$$$$$$$$ " + e);
//            if (e instanceof NullPointerException) {
//                saveCheckImeiFailDetails(checkImeiRequest, startTime, nullPointerException);
//            } else if (e instanceof SQLException) {
//                saveCheckImeiFailDetails(checkImeiRequest, startTime, sQLException);
//            } else {
//                saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());
//            }  //  else if (e instanceof SQLGrammarException) { saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());            }
//            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
//            logger.error("Failed at " + e.getLocalizedMessage());
//            saveCheckImeiRequest(checkImeiRequest, startTime);
//            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
//        }
//    }