package com.gl.ceir.config.service.impl;

import com.gl.RuleEngineAdaptor;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.*;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.model.constants.CustomCheckImeiRequest;
import com.gl.ceir.config.repository.app.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
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
public class CustomImeiCheckServiceImpl {

    private static final Logger logger = LogManager.getLogger(CustomImeiCheckServiceImpl.class);

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
    CheckImeiServiceSendSMS checkImeiServiceSendSMS;

    @Autowired
    NationalWhitelistRepository nationalWhitelistRepository;

    @Autowired
    SystemConfigurationDbRepository systemConfigurationDbRepositry;

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
    GdceRegisterImeiReqRepo gdceRegisterImeiReqRepo;


    public List<CustomImeiCheckResponse> startCustomCheckService(List<CustomCheckImeiRequest> custChckImeiReq, GdceCheckImeiReq obj) {
        List<CustomImeiCheckResponse> imeiResponse = new LinkedList<>();
        try {
            int successCount = 0;
            int failCount = 0;
        var conn = dbRepository.getConnection();
        String userIp = request.getHeader("HTTP_CLIENT_IP") == null ? (request.getHeader("X-FORWARDED-FOR") == null ? request.getRemoteAddr() : request.getHeader("X-FORWARDED-FOR")) : request.getHeader("HTTP_CLIENT_IP");
        String userAgent = request.getHeader("user-agent");
        var startTime = System.currentTimeMillis();
            for (CustomCheckImeiRequest cusReq : custChckImeiReq) {
                logger.info("********Starting Check imei for" + cusReq);
                try {
                    var checkImeiRequest = new CheckImeiRequest(cusReq.getImei(), "API", userAgent, userIp, obj.getRequestId());

                    if (StringUtils.isBlank(cusReq.getImei()) || StringUtils.isBlank(cusReq.getSerialNumber())) {
                        logger.info("imei/sno is not present for {} ", cusReq);
                        imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", ""));
                        checkImeiRequest.setImeiProcessStatus("Invalid");
                        checkImeiRequest.setFail_process_description(mandatoryParameterMissing);
                        checkImeiRequest.setComplianceValue(0);
                        checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg); //"Device is not-compliant"
                        failCount++;
                    } else if (cusReq.getImei().length() < 14 || cusReq.getImei().length() > 20 || !cusReq.getImei().matches("^[ 0-9 ]+$")) {
                        imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", ""));
                        checkImeiRequest.setImeiProcessStatus("Invalid");
                        checkImeiRequest.setFail_process_description(imeiInvalid_Msg);
                        checkImeiRequest.setComplianceValue(0);
                        checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg); //"Device is not-compliant"
                        failCount++;
                    } else {
                        var deviceInfo = Map.of("appdbName", "app", "auddbName", "aud", "repdbName", "rep", "edrappdbName", "edrapp", "userType", "default", "imei", cusReq.getImei(), "feature", "CustomCheckImei");
                        LinkedHashMap<String, Boolean> rules = RuleEngineAdaptor.startAdaptor(conn, deviceInfo);
                        logger.info("Rules Return " + rules);
                        Map.Entry<String, Boolean> lastEntry = rules.entrySet().stream().skip(rules.size() - 1).findFirst().get();
                        logger.info("Finale rule-> " + lastEntry.getKey() + " with value ->" + lastEntry.getValue());
                        if (lastEntry.getValue()) {
                            var tacDetail = gsmaTacDetailsRepository.getBydeviceId(cusReq.getImei().substring(0, 8));
                            logger.info("tacDetail" + tacDetail);
                            if (tacDetail == null) {
                                imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", ""));
                                checkImeiRequest.setImeiProcessStatus("Invalid");
                                checkImeiRequest.setFail_process_description(applicationContext.getEnvironment().getProperty("MDR_Msg"));
                                checkImeiRequest.setComplianceValue(0);
                                failCount++;
                                checkImeiRequest.setComplianceStatus(deviceNotCompliantMsg); //"Device is not-compliant"
                            } else {
                                imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "200", deviceCompliantMsg, tacDetail.getDevice_type(), tacDetail.getBrand_name(), tacDetail.getModel_name()));
                                checkImeiRequest.setImeiProcessStatus("Valid");
                                checkImeiRequest.setComplianceValue(1);
                                successCount++;
                                checkImeiRequest.setComplianceStatus(deviceCompliantMsg); //"Device is Compliant"
                            }
                        } else {
                            failCount++;
                            imeiResponse.add(new CustomImeiCheckResponse(cusReq.getImei(), cusReq.getSerialNumber(), "201", deviceNotCompliantMsg, "", "", ""));
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
                    logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CustomImeiCheckServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
                }
            }
            obj.setFailCount(failCount);
            obj.setSuccessCount(successCount);
            obj.setStatus("Success");
            gdceCheckImeiReqRepository.save(obj);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CustomImeiCheckServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
        }
        createFile(Arrays.toString(imeiResponse.toArray()), "checkIMEI", "resp", obj.getRequestId());
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


    //********************************************************
    //********************************************************
    //********************************************************

    public List<ResponseArray> registerService(List<GdceData> gdceData, GdceRegisterImeiReq obj) {
        var conn = dbRepository.getConnection();
        int failCount = 0;
        int passCount = 0;
        var startTime = System.currentTimeMillis();
        List<ResponseArray> responseArray = new LinkedList<>();
        List<PrintReponse> a = new LinkedList<>();
        try {
            for (GdceData gdData : gdceData) {
                logger.info("********Starting Registering for" + gdceData);
                if (StringUtils.isBlank(gdData.getImei()) ||
                        StringUtils.isBlank(gdData.getSerial_number()) ||
                        StringUtils.isBlank(gdData.getGoods_description()) ||
                        StringUtils.isBlank(gdData.getCustoms_duty_tax())
                        || StringUtils.isBlank(gdData.getDevice_type()) ||
                        StringUtils.isBlank(gdData.getBrand()) ||
                        StringUtils.isBlank(gdData.getModel()) ||
                        gdData.getSim() == 0) {
                    logger.info("Mandatory param missing for " + gdData);
                    responseArray.add(new ResponseArray(gdData.getImei(), gdData.getSerial_number(), 202, failMessage));
                    a.add(new PrintReponse(gdData.getImei(), gdData.getSerial_number(), 202, failMessage, mandatoryParameterMissing));
                    failCount++;
                }
                if (gdData.getImei().length() < 14 || gdData.getImei().length() > 20 ||
                        !gdData.getImei().matches("^[ 0-9 ]+$")) {
                    logger.info("imei not valid : " + gdData.getImei());
                    responseArray.add(new ResponseArray(gdData.getImei(), gdData.getSerial_number(), 202, failMessage));
                    a.add(new PrintReponse(gdData.getImei(), gdData.getSerial_number(), 202, failMessage, imeiInvalid_Msg));
                    failCount++;
                } else {
                    var deviceInfo = Map.of("appdbName", "app", "auddbName", "aud", "repdbName", "rep", "edrappdbName", "edrapp", "userType", "default", "imei", gdData.getImei(), "feature", "CustomRegisterImei", "source", customSource);
                    LinkedHashMap<String, Boolean> rules = RuleEngineAdaptor.startAdaptor(conn, deviceInfo);
                    logger.info("Rules Return " + rules);
                    var lastRule = rules.entrySet().stream().map(Map.Entry::getKey).reduce((first, second) -> second).orElse(null);  // optimse
                    var response = rules.entrySet().stream().map(Map.Entry::getValue).reduce((first, second) -> second).orElse(null); // opt
                    logger.info("Finale " + lastRule + "  rule-> " + response);
                    if (!response) {
                        if (rules.containsKey("CUSTOM_GDCE")) {
                            responseArray.add(new ResponseArray(gdData.getImei(), gdData.getSerial_number(), 201, gdFailMessage));
                            a.add(new PrintReponse(gdData.getImei(), gdData.getSerial_number(), 201, gdFailMessage, applicationContext.getEnvironment().getProperty(lastRule+ "_Msg")));
                        } else {
                            responseArray.add(new ResponseArray(gdData.getImei(), gdData.getSerial_number(), 202, failMessage));
                            a.add(new PrintReponse(gdData.getImei(), gdData.getSerial_number(), 202, failMessage,  applicationContext.getEnvironment().getProperty(lastRule+ "_Msg")));
                        }
                    } else {
                        gdData.setRequest_id(obj.getRequestId());
                        logger.info("Request Pass for  " + gdData.getImei());
                        addInImeiPairDetail(gdData);
                        addInExceptionList(gdData);
                        addInBlackList(gdData);
                        if (insertInGdceData(gdData)) {
                            passCount++;
                            responseArray.add(new ResponseArray(gdData.getImei(), gdData.getSerial_number(), 200, passMessage));
                            a.add(new PrintReponse(gdData.getImei(), gdData.getSerial_number(), 200, passMessage, "Pass"));
                        } else {
                            failCount++;
                            responseArray.add(new ResponseArray(gdData.getImei(), gdData.getSerial_number(), 202, failMessage));
                            a.add(new PrintReponse(gdData.getImei(), gdData.getSerial_number(), 202, failMessage, "Fail to insert in gdce_data"));
                        }
                    }
                }
            }
            updateGdceRegister(passCount, failCount, obj);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CustomImeiCheckServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
        }
        createFile(Arrays.toString(a.toArray()), "registerIMEI", "resp", obj.getRequestId());
        return responseArray;
    }

    private void updateGdceRegister(int passCount, int failCount, GdceRegisterImeiReq obj) {
        obj.setRemark("200");
        obj.setStatus("Success");
        obj.setFailCount(failCount);
        obj.setSuccessCount(passCount);
        gdceRegisterImeiReqRepo.save(obj);
    }

    @Transactional
    private void addInImeiPairDetail(GdceData gdData) {
        try {
            List<ImeiPairDetail> pairs = imeiPairDetailRepo.getByImei(gdData.getImei());
            for (ImeiPairDetail p : pairs) {
                imeiPairDetailHisRepo.save(new ImeiPairDetailHis(p.getAllowedDays(), p.getImei(), p.getImsi(), p.getMsisdn(), p.getFilename(), p.getGsmaStatus(), p.getPairMode(), p.getOperator(), "2", p.getPairingDate(), p.getRecordTime(), p.getExpiryDate(), "GDCE_TAX_PAID"));
            }
            imeiPairDetailRepo.deleteByImei(gdData.getImei());
        } catch (Exception e) {
            logger.warn("Not able to insert/update in imei_pair/his, Exception :{}", e.getLocalizedMessage());
        }
    }

    @Transactional
    private void addInExceptionList(GdceData gdData) {
        try {
            for (ExceptionList e : exceptionListRepo.getByImei(gdData.getImei())) {
                exceptionListHisRepo.save(new ExceptionListHist(e.getComplaintType(), e.getImei(), e.getModeType(), e.getRequestType(), e.getTxnId(), e.getUserId(), e.getUserType(), e.getOperatorId(), e.getOperatorName(), e.getActualImei(), e.getTac(), e.getRemarks(), e.getImsi(), e.getMsisdn(), e.getSource(), e.getExpiryDate(), "GDCE_TAX_PAID"));
            }
            exceptionListRepo.deleteByImei(gdData.getImei());
        } catch (Exception e) {
            logger.warn("Not able to insert/update in exception/his, Exception :{}", e.getLocalizedMessage());
        }
    }

    @Transactional
    private void addInBlackList(GdceData gdData) {
        try {
            for (BlackList b : blackListRepository.getByImei(gdData.getImei())) {
                blackListHisRepo.save(new BlackListHis(b.getActualImei(), b.getComplaintType(), b.getImei(), b.getImsi(), b.getModeType(), b.getMsisdn(), b.getOperatorId(), b.getOperatorName(), b.getRemarks(), b.getRequestType(), b.getSource(), b.getTac(), b.getTxnId(), b.getUserId(), b.getUserType(), "GDCE_TAX_PAID"));
            }
            logger.info("Going to Exit   ");
            blackListRepository.deleteByImei(gdData.getImei());
        } catch (Exception e) {
            logger.warn("Not able to insert/update in blacklist/his, Exception :{}", e.getLocalizedMessage());
        }
    }

    private boolean insertInGdceData(GdceData gdData) {
        try {
            gdData.setActual_imei(gdData.getImei());
            gdData.setImei(gdData.getImei().substring(0, 14));
            gdData.setIsCustomTaxPaid(gdData.getCustoms_duty_tax().equals("paid") ? 1 : 0);
            gdData.setSource("GDCE");
            gdceDataRepository.save(gdData);
            return true;
        } catch (Exception e) {
            logger.warn("Not able to insert in gdce_data, Exception :{}", e.getLocalizedMessage());
            return false;
        }

    }

    public String createFile(String prm, String feature, String type, String reqId) {
        try {
            var filepath = systemConfigurationDbRepositry.getByTag("CustomApiFilePath").getValue() + "/" + feature + "/" + reqId + "/";
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


    public String createFileForCustomCheck(List<CustomCheckImeiRequest> list, String reqId) {
        try {
            var filepath = systemConfigurationDbRepositry.getByTag("CustomApiFilePath").getValue() + "/checkIMEI/" + reqId + "/";
            Files.createDirectories(Paths.get(filepath));
            logger.info("FullFilePath--" + filepath);

            FileWriter writer = new FileWriter(filepath + reqId + ".txt");
            writer.write(Arrays.toString(list.toArray()));
            writer.close();
            return reqId + ".txt";
        } catch (Exception e) {
            logger.error("Not able to create custom file {}", e.getLocalizedMessage());
        }
        return null;
    }

    public String createFileForRegisterCustom(List<GdceData> list, String reqId) {
        try {
            var filepath = systemConfigurationDbRepositry.getByTag("CustomApiFilePath").getValue() + "/registerIMEI/" + reqId + "/";
            Files.createDirectories(Paths.get(filepath));
            logger.info("FullFilePath--" + filepath);
            FileWriter writer = new FileWriter(filepath + reqId + ".txt");
            writer.write(Arrays.toString(list.toArray()));
            writer.close();
            return reqId + ".txt";
        } catch (Exception e) {
            logger.error(" Not able to create register file {}", e.toString());
        }
        return null;
    }


}


class PrintReponse {
    String imei;
    String serialNumber;
    int statusCode;
    String statusMessage;
    String response;

    public PrintReponse(String imei, String serialNumber, int statusCode, String statusMessage, String response) {
        this.imei = imei;
        this.serialNumber = serialNumber;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.response = response;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public PrintReponse() {
    }

    @Override
    public String toString() {
        return "{" + "imei='" + imei + '\'' + ", serialNumber='" + serialNumber + '\'' + ", statusCode=" + statusCode + ", statusMessage='" + statusMessage + '\'' + ", response='" + response + '\'' + '}';
    }
}

class ResponseArray {

    String imei;
    String serialNumber;
    int statusCode;
    String statusMessage;

    public ResponseArray() {
    }

    public ResponseArray(String imei, String serialNumber, int statusCode, String statusMessage) {
        this.imei = imei;
        this.serialNumber = serialNumber;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return "{" + "imei='" + imei + '\'' + ", serialNumber='" + serialNumber + '\'' + ", statusCode=" + statusCode + ", statusMessage='" + statusMessage + '\'' + '}';
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