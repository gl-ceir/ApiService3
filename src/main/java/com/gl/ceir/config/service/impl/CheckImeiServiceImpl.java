package com.gl.ceir.config.service.impl;

import com.gl.RuleEngineAdaptor;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.app.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class CheckImeiServiceImpl {

    private static final Logger logger = LogManager.getLogger(CheckImeiServiceImpl.class);

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
    CheckImeiResponseParamRepository responseRepo;

    @Autowired
    GsmaTacDetailsRepository gsmaTacDetailsRepository;

    @Autowired
    CheckImeiRequestRepository checkImeiRequestRepository;

    @Autowired
    LanguageLabelDbRepository languageLabelDbRepository;

    @Autowired
    CheckImeiServiceSendSMS checkImeiServiceSendSMS;

    @Autowired
    DbRepository dbRepository;

    public CheckImeiResponse getImeiDetailsDevicesNew(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            var rules = getResponseStatusViaRuleEngine(checkImeiRequest);
            var responseTag = "CheckImeiResponse_" + checkImeiRequest.getComplianceValue();  // optimise
            logger.info("Response Tag :: " + responseTag);
            var result = getResult(checkImeiRequest, rules, responseTag);
            var response = saveCheckImeiRequest(checkImeiRequest, startTime);
            checkImeiServiceSendSMS.sendSMSforUSSD_SMS(checkImeiRequest, responseTag, response);
            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()), StatusMessage.FOUND.getName(), checkImeiRequest.getLanguage(), result);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CheckImeiServiceImpl.class.getName())).collect(Collectors.toList()).get(0) + "]");
            logger.error("Failed at " + e.getLocalizedMessage() + " ----- " + e.toString() + " ::::: " + e.getMessage() + " $$$$$$$$$$$ " + e);
            if (e instanceof NullPointerException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, nullPointerException);
            } else if (e instanceof SQLException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, sQLException);
            } else {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());
            }  //  else if (e instanceof SQLGrammarException) { saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());            }
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
            logger.error("Failed at " + e.getLocalizedMessage());
            saveCheckImeiRequest(checkImeiRequest, startTime);
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    private LinkedHashMap<String, Boolean> getResponseStatusViaRuleEngine(CheckImeiRequest checkImeiRequest) {
        int complianceValue = 0;
        Connection conn = dbRepository.getConnection();
        var deviceInfo = Map.of("appdbName", "app", "auddbName", "aud", "repdbName", "rep", "edrappdbName", "edrapp",
                "imei", checkImeiRequest.getImei(),
                "msisdn", checkImeiRequest.getMsisdn() == null ? "" : checkImeiRequest.getMsisdn(),
                "imsi", checkImeiRequest.getImsi() == null ? "" : checkImeiRequest.getImsi(),
                "feature", "CheckImei", "operator", checkImeiRequest.getOperator() == null ? "" : checkImeiRequest.getOperator());
        var startTime = System.currentTimeMillis();
        LinkedHashMap<String, Boolean> rules = RuleEngineAdaptor.startAdaptor(conn, deviceInfo);
        logger.info("RuleEngine Time Taken is  :->" + (System.currentTimeMillis() - startTime));

        if (!rules.get("MDR")) {
            complianceValue = rules.get("NWL") ? 9 : 10;
        } else if (!rules.get("NWL")) {
            if (rules.get("CUSTOM_CHK")) {
                complianceValue = rules.get("TRC") ? 5 : 6;
            } else {
                complianceValue = rules.get("TRC") ? 7 : 8;
            }
        } else {
            if (rules.get("CUSTOM_CHK")) {
                complianceValue = rules.get("TRC") ? 1 : 2;
            } else {
                complianceValue = rules.get("TRC") ? 3 : 4;
            }
        }
        checkImeiRequest.setComplianceValue(complianceValue);
        logger.info("ComplianceValue :->" + complianceValue + ",For Channel :->" + checkImeiRequest.getChannel() + ", For Language :->" + checkImeiRequest.getLanguage());
        return rules;
    }

    private Result getResult(CheckImeiRequest checkImeiRequest, LinkedHashMap<String, Boolean> rules, String status) {

        LinkedHashMap mappedDeviceDetails = null;
        if (!(checkImeiRequest.getComplianceValue() == 9 || checkImeiRequest.getComplianceValue() == 10)) {
            var gsmaTacDetails = gsmaTacDetailsRepository.getBydeviceId(checkImeiRequest.getImei().substring(0, 8));
            mappedDeviceDetails = deviceDetailsNew(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), checkImeiRequest.getLanguage());
        }
        var message = responseRepo.getByTagAndLanguage(
                        checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? status + "ForUssd" : checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                                ? status + "ForSms" : status,
                        checkImeiRequest.getLanguage())
                .getValue()
                .replace("<imei>", checkImeiRequest.getImei());
        var compStatus = responseRepo.getByTagAndLanguage(
                checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? status + "_ComplianceForUssd" :
                        checkImeiRequest.getChannel().equalsIgnoreCase("sms") ? status + "_ComplianceForSms" : status + "_Compliance",
                checkImeiRequest.getLanguage());
        String remarksValue = "Remarks:";// get from app.prop

        // for (String str : List.of("IMEI_PAIRING", "STOLEN", "DUPLICATE_DEVICE", "EXIST_IN_BLACKLIST_DB"  , "SMPL"  ))  {  //, "EXISTS_IN_GREYLIST_DB"
        for (String str : remarkRules) {  //, "EXISTS_IN_GREYLIST_DB"
            var remarkTag = "CheckImeiRemark_" + str + "_" + rules.get(str);
            logger.info("Remarks  :->" + remarkTag + "::::" + rules.get(str));
            var chkImeiResParam = responseRepo.getByTagAndLanguage(
                    checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? remarkTag + "ForUssd" :
                            checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                                    ? remarkTag + "ForSms" : remarkTag,
                    checkImeiRequest.getLanguage());
            var response = chkImeiResParam == null ? "" : chkImeiResParam.getValue().replace("<imei>", checkImeiRequest.getImei());
            remarksValue += (response.isEmpty() || response.equals("")) ? "" : (response + ",");
        }
        remarksValue = remarksValue.substring(0, remarksValue.length() - 1);
        if (remarksValue.equalsIgnoreCase("Remarks"))
            remarksValue = "";
        var complianceStatus = compStatus == null ? null : compStatus.getValue().replace("<imei>", checkImeiRequest.getImei()) + " " + remarksValue;
        logger.info("Compliance Status:::->" + complianceStatus + "MDR Response  :->" + mappedDeviceDetails);
        var symbolTag = status + "_SymbolColor";
        var symbolResponse = responseRepo.getByTagAndFeatureName(symbolTag, "CheckImei");    //  message, deviceDetails == null ? null :
        logger.info("SymbolColor Response :::->" + symbolResponse.toString());
        var symbolColor = symbolResponse.getValue();
        var isValidImei = false;
        if (checkImeiRequest.getComplianceValue() == 1 || checkImeiRequest.getComplianceValue() == 5)
            isValidImei = true;
        checkImeiRequest.setImeiProcessStatus(isValidImei == true ? "Valid" : "Invalid");
        checkImeiRequest.setComplianceStatus(complianceStatus);
        checkImeiRequest.setSymbol_color(symbolColor);
        checkImeiRequest.setRequestProcessStatus("Success");
        var result = new Result(isValidImei, symbolColor, complianceStatus, message, mappedDeviceDetails == null ? null : mappedDeviceDetails);
        return result;
    }


    public void saveCheckImeiFailDetails(CheckImeiRequest checkImeiRequest, long startTime, String desc) {
        checkImeiRequest.setRequestProcessStatus("Fail");
        checkImeiRequest.setFail_process_description(desc);
        // alertServiceImpl.raiseAnAlert(Alerts.ALERT_1110.getName(), desc, "Check Imei ", 0);
        logger.info(" CHECK_IMEI :Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + ", Response =" + desc);
        saveCheckImeiRequest(checkImeiRequest, startTime);
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
        return responseRepo.getByTagAndLanguage("CheckImeiErrorMessage", language).getValue();
    }

    public String checkImeiServiceDownMsg(String language) {
        return responseRepo.getByTagAndLanguage("CheckImeiServiceDownMessage", language).getValue();
    }

    private LinkedHashMap deviceDetailsNew(String brand_name, String model_name, String device_type, String manufacturer, String marketing_name, String lang) {
        LinkedHashMap<String, String> item = new LinkedHashMap();
        item.put(responseRepo.getByTagAndLanguage("brandName", lang).getValue(), brand_name);
        item.put(responseRepo.getByTagAndLanguage("modelName", lang).getValue(), model_name);
        item.put(responseRepo.getByTagAndLanguage("manufacturer", lang).getValue(), manufacturer);
        item.put(responseRepo.getByTagAndLanguage("marketingName", lang).getValue(), marketing_name);
        item.put(responseRepo.getByTagAndLanguage("deviceType", lang).getValue(), device_type);
        return item;
    }

}
