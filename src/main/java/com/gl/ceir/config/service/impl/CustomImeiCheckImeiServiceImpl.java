package com.gl.ceir.config.service.impl;

import com.gl.RuleEngineAdaptor;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CustomImeiCheckResponse;
import com.gl.ceir.config.model.app.GdceCheckImeiReq;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.model.constants.CustomCheckImeiRequest;
import com.gl.ceir.config.repository.app.*;
import com.gl.custom.CustomCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class CustomImeiCheckImeiServiceImpl {

    private static final Logger logger = LogManager.getLogger(CustomImeiRegisterServiceImpl.class);

    @Value("${nullPointerException}")
    private String nullPointerException;

    @Value("${sqlException}")
    private String sQLException;

    @Value("${deviceNotCompliantMsg}")
    private String deviceNotCompliantMsg;

    @Value("${deviceCompliantMsg}")
    private String deviceCompliantMsg;

    @Value("${ruleResponseError}")
    private String ruleResponseError;

    @Value("${someWentWrongException}")
    private String someWentWrongException;


    @Value("${customSource}")
    private String customSource;

    @Value("${gdFailMessage}")
    private String gdFailMessage;

    @Value("${failMessage}")
    private String failMessage;

    @Value("${passMessage}")
    private String passMessage;


    @Value("${imeiInvalid_Msg}")
    private String imeiInvalid_Msg;

    @Value("${mandatoryParameterMissing}")
    private String mandatoryParameterMissing;

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
    DbRepository dbRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    BlackListHisRepo blackListHisRepo;
    @Autowired
    BlackListRepository blackListRepository;
    @Autowired
    ExceptionListRepo exceptionListRepo;
    @Autowired
    ExceptionListHisRepo exceptionListHisRepo;
    @Autowired
    ImeiPairDetailRepo imeiPairDetailRepo;
    @Autowired
    ImeiPairDetailHisRepo imeiPairDetailHisRepo;
    @Autowired
    GdceDataRepository gdceDataRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    CustomCheck customCheck;
    @Autowired
    SystemParamServiceImpl systemParamServiceImpl;


    public List<CustomImeiCheckResponse> startCustomCheckService(List<CustomCheckImeiRequest> custChckImeiReq, GdceCheckImeiReq obj) {
        List<CustomImeiCheckResponse> imeiResponse = new LinkedList<>();
        try {
            int successCount = 0;
            int failCount = 0;
            String userIp = request.getHeader("HTTP_CLIENT_IP") == null ? (request.getHeader("X-FORWARDED-FOR") == null ? request.getRemoteAddr() : request.getHeader("X-FORWARDED-FOR")) : request.getHeader("HTTP_CLIENT_IP");
            String userAgent = request.getHeader("user-agent");
            var startTime = System.currentTimeMillis();
            for (CustomCheckImeiRequest cusReq : custChckImeiReq) {
                logger.info("********Starting Check imei for" + cusReq);
                try {
                    var checkImeiRequest = new CheckImeiRequest(cusReq.getImei(), "API", userAgent, userIp, obj.getRequestId());

                    if (StringUtils.isBlank(cusReq.getImei()) ) {
                        logger.info("imei/sno is not present for {} ", cusReq);
                        imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", "","",""));
                        checkImeiRequest.setImeiProcessStatus("Invalid");
                        checkImeiRequest.setFail_process_description(mandatoryParameterMissing);
                        checkImeiRequest.setComplianceValue(0);
                        checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg); //"Device is not-compliant"
                        failCount++;
                    } else if (cusReq.getImei().length() < 14 || cusReq.getImei().length() > 20 || !cusReq.getImei().matches("^[ 0-9 ]+$")) {
                        imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", "" ,"",""));
                        checkImeiRequest.setImeiProcessStatus("Invalid");
                        checkImeiRequest.setFail_process_description(imeiInvalid_Msg);
                        checkImeiRequest.setComplianceValue(0);
                        checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg); //"Device is not-compliant"
                        failCount++;
                    } else {
                        var deviceInfo = Map.of("appdbName", "app", "auddbName", "aud", "repdbName", "rep", "edrappdbName", "edrapp",
                                "userType", "default", "imei", cusReq.getImei(), "feature", "CustomCheckImei");
                        LinkedHashMap<String, Boolean> rules = null;
                        try (var conn = dbRepository.getConnection()) {
                            rules = RuleEngineAdaptor.startAdaptor(conn, deviceInfo);
                        }
                        logger.info("Rules Return " + rules);
                        Map.Entry<String, Boolean> lastEntry = rules.entrySet().stream().skip(rules.size() - 1).findFirst().get();
                        logger.info("Finale rule-> " + lastEntry.getKey() + " with value ->" + lastEntry.getValue());

                        if (lastEntry.getValue()) {
                            var tacDetail = gsmaTacDetailsRepository.getBydeviceId(cusReq.getImei().substring(0, 8));
                            logger.info("tacDetail" + tacDetail);
                            if (tacDetail == null) {
                                imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", "" ,"",""));
                                checkImeiRequest.setImeiProcessStatus("Invalid");
                                checkImeiRequest.setFail_process_description(applicationContext.getEnvironment().getProperty("MDR_Msg"));
                                checkImeiRequest.setComplianceValue(0);
                                failCount++;
                                checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg); //"Device is not-compliant"
                            } else {
                                imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "200", deviceCompliantMsg, tacDetail.getDevice_type(), tacDetail.getBrand_name(), tacDetail.getModel_name() , tacDetail.getMarketing_name() ,tacDetail.getManufacturer()  ));
                                checkImeiRequest.setImeiProcessStatus("Valid");
                                checkImeiRequest.setComplianceValue(1);
                                successCount++;
                                checkImeiRequest.setComplianceStatus(deviceCompliantMsg); //"Device is Compliant"
                            }
                        } else {
                            failCount++;
                            imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", "","",""));
                            checkImeiRequest.setImeiProcessStatus("Invalid");
                            checkImeiRequest.setComplianceValue(1);
                            String value = applicationContext.getEnvironment().getProperty(lastEntry.getKey() + "_Msg");
                            logger.info("Env value for {} is {}  ", lastEntry.getKey(), value);
                            checkImeiRequest.setFail_process_description(value);
                            checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg);
                        }
                    }
                    checkImeiRequest.setRequestProcessStatus("Success");
                    saveCheckImeiRequest(checkImeiRequest, startTime);
                } catch (Exception e) {
                    logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CustomImeiRegisterServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
                }
            }
            obj.setFailCount(failCount);
            obj.setSuccessCount(successCount);
            obj.setStatus("Success");
            gdceCheckImeiReqRepository.save(obj);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CustomImeiRegisterServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
        }
        createFile(Arrays.toString(imeiResponse.toArray()), "checkIMEI", "response", obj.getRequestId());
        return imeiResponse;
    }

    public CheckImeiRequest saveCheckImeiRequest(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            checkImeiRequest.setCheckProcessTime(String.valueOf(System.currentTimeMillis() - startTime));
            return checkImeiRequestRepository.save(checkImeiRequest);
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1110.getName(), 0);
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    public String globalErrorMsgs(String language) {
        return chkImeiRespPrmRepo.getByTagAndLanguage("CheckImeiErrorMessage", language).getValue();
    }

    public String createFile(String prm, String feature, String type, String reqId) {
        try {
            var filepath = systemParamServiceImpl.getValueByTag("CustomApiFilePath") + "/" + feature + "/" + reqId + "/";
            Files.createDirectories(Paths.get(filepath));
            logger.info("FullFilePath--" + filepath);
            FileWriter writer = new FileWriter(filepath + reqId + "_" + type + ".txt");
            writer.write(prm);
            writer.close();
            return reqId + "_" + type + ".txt";
        } catch (Exception e) {
            logger.error("Not able to create custom file {}", e.getLocalizedMessage());
        }
        return null;
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