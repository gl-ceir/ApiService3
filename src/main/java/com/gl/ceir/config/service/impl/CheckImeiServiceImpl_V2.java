package com.gl.ceir.config.service.impl;

import com.gl.RuleEngineAdaptor;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.app.CheckImeiRequestRepository;
import com.gl.ceir.config.repository.app.CheckImeiResponseParamRepository;
import com.gl.ceir.config.repository.app.DbRepository;
import com.gl.ceir.config.repository.app.GsmaTacDetailsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
public class CheckImeiServiceImpl_V2 {

    private static final Logger logger = LogManager.getLogger(CheckImeiServiceImpl_V2.class);

    @Value("${nullPointerException}")
    private String nullPointerException;

    @Value("${sqlException}")
    private String sQLException;

    @Value("${ruleResponseError}")
    private String ruleResponseError;

    @Value("${someWentWrongException}")
    private String someWentWrongException;

    @Value("${stolenRule}")
    private String stolenRule;

    @Value("#{'${combinationRules}'.split(',')}")
    public List<String> combinationRules;

    @Value("${appdbName}")
    private   String appdbName;

    @Value("${auddbName}")
    private   String auddbName;

    @Value("${repdbName}")
    private   String repdbName;

    @Value("${edrappdbName}")
    private   String edrappdbName;

    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    CheckImeiResponseParamRepository chkImeiRespPrmRepo;

    @Autowired
    CheckImeiResponseParamRepoImpl cachedResponse;

    @Autowired
    GsmaTacDetailsRepository gsmaTacDetailsRepository;

    @Autowired
    CheckImeiRequestRepository checkImeiRequestRepository;

    @Autowired
    CheckImeiServiceSendSMS checkImeiServiceSendSMS;

    @Autowired
    DbRepository dbRepository;

    @Autowired
    SystemParamServiceImpl systemParamServiceImpl;


    public CheckImeiResponse getImeiDetailsDevicesNew(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            logger.info("Starting ....... ");
            LinkedHashMap<String, Boolean> rules = getRuleListResponseFromRuleEngine(checkImeiRequest);
            var responseTag = getFinalResponseTag(rules);
            logger.info("Response Tag :: {} ", responseTag);
            var result = getResult(checkImeiRequest, rules, responseTag);
            checkImeiServiceSendSMS.sendSMSforUSSD_SMS(checkImeiRequest, responseTag, saveCheckImeiRequest(checkImeiRequest, startTime));
            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()), StatusMessage.FOUND.getName(), checkImeiRequest.getLanguage(), result);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(CheckImeiServiceImpl_V2.class.getName())).collect(Collectors.toList()).get(0) + "]");
            if (e instanceof NullPointerException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, nullPointerException);
            } else if (e instanceof SQLException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, sQLException);
            } else {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());
            }  //  else if (e instanceof SQLGrammarException) { saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());            }
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
            logger.error("Failed at " + e.getLocalizedMessage());
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    private String getFinalResponseTag(LinkedHashMap<String, Boolean> r) {
        var lastRule = r.keySet().stream().reduce((first, second) -> second).orElse(null);  // opt
        logger.info("Last Rule :{} and its value {}", lastRule, r.get(lastRule));
        return combinationRules.contains(lastRule) ? getTagFromCombination(r) : lastRule;
    }

    private String getTagFromCombination(LinkedHashMap<String, Boolean> r) {
        String value = "";
        boolean isWhitelisted = r.getOrDefault("NATIONAL_WHITELISTS", true);
        boolean isNWLValid = r.getOrDefault("NWL_VALIDITYFLAG", true);
        boolean isCustomManufacturer = r.getOrDefault("CUSTOM_LOCAL_MANUFACTURER", true);
        boolean isMDR = r.getOrDefault("MDR", true);
        logger.info(" isWhitelisted- {}, isNWLValid- {} , isCustomManufacturer -{},   isMDR -{}", isWhitelisted, isNWLValid, isCustomManufacturer, isMDR);
        if (isWhitelisted) {
            value = isNWLValid ? "eirs_compliant_registered" : "eirs_not_compliant_registered";
        } else {
            value = isMDR ? (isCustomManufacturer ? "eirs_compliant_not_registered" : "eirs_not_gdce_compliant_not_registered") : "eirs_not_compliant_not_registered";
        }
        return value;

    }

    private LinkedHashMap<String, Boolean> getRuleListResponseFromRuleEngine(CheckImeiRequest checkImeiRequest) {
        try {
             var startTime = System.currentTimeMillis();
            logger.info("Rule  deviceInfo {} :-", getDeviceInfoMap(checkImeiRequest));
            LinkedHashMap<String, Boolean> rules = RuleEngineAdaptor.startAdaptor(getDeviceInfoMap(checkImeiRequest));
            logger.info("Rule  Time Taken is  :->{} . [] response  {}", System.currentTimeMillis() - startTime, rules);
            return rules;
        } catch (Exception e) {
            logger.error(e + "Not able to get rules {}", e.getMessage());
            return null;
        }
    }

    private static Map<String, String> getDeviceInfoMap(CheckImeiRequest checkImeiRequest) {
       var map = Map.of("appdbName", "app", "auddbName", "aud", "repdbName", "rep", "edrappdbName", "app_edr",
                "userType", "default", "imei", checkImeiRequest.getImei(), "msisdn", checkImeiRequest.getMsisdn() == null ? "" : checkImeiRequest.getMsisdn(),
               "imsi", checkImeiRequest.getImsi() == null ? "" : checkImeiRequest.getImsi(),
                "feature", "CheckImeiNew", "operator", checkImeiRequest.getOperator() == null ? "" : checkImeiRequest.getOperator());
        return map;
    }

    private Result getResult(CheckImeiRequest checkImeiRequest, LinkedHashMap<String, Boolean> rules, String responseTag) {
        var message = getMessage(checkImeiRequest, responseTag);
        var symbolColor = getSymbolColor(checkImeiRequest, responseTag);
        var devDetails = getDeviceDetails(responseTag, checkImeiRequest);
        getComplinaceStatus(checkImeiRequest, responseTag);
        checkImeiRequest.setRequestProcessStatus("Success");
        return new Result(checkImeiRequest.getImeiProcessStatus().equalsIgnoreCase("valid"),
                symbolColor,
                checkImeiRequest.getComplianceStatus(),
                message,
                devDetails);
    }

    private String getMessage(CheckImeiRequest checkImeiRequest, String responseTag) {
        return chkImeiRespPrmRepo.getByTagAndLanguage(checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? responseTag + "_MsgForUssd" : checkImeiRequest.getChannel().equalsIgnoreCase("sms") ? responseTag + "_MsgForSms" : responseTag + "_Msg", checkImeiRequest.getLanguage()).getValue().replace("<imei>", checkImeiRequest.getImei());
    }

    private String getSymbolColor(CheckImeiRequest checkImeiRequest, String responseTag) {
        var symbolTag = responseTag + "_SymbolColor";
        var symbolResponse = chkImeiRespPrmRepo.getByTagAndFeatureName(symbolTag, "CheckImei");    //  mes sage, deviceDetails == null ? null :
        logger.info("SymbolColor Response :::->" + symbolResponse.toString());
        var symbolColor = symbolResponse.getValue();
        checkImeiRequest.setSymbol_color(symbolColor);
        return symbolColor;
    }

    private void getComplinaceStatus(CheckImeiRequest checkImeiRequest, String responseTag) {
        var compStatus = chkImeiRespPrmRepo.getByTagAndLanguage(checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? responseTag + "_ComplianceForUssd" : checkImeiRequest.getChannel().equalsIgnoreCase("sms") ? responseTag + "_ComplianceForSms" : responseTag + "_Compliance", checkImeiRequest.getLanguage());
        var complianceStatus = compStatus == null ? null : compStatus.getValue().replace("<imei>", checkImeiRequest.getImei());
        logger.info("Compliance Status:::->" + complianceStatus);
        checkImeiRequest.setComplianceStatus(complianceStatus);
    }


    public LinkedHashMap<String, String> getDeviceDetails(String tag, CheckImeiRequest checkImeiRequest) {
        LinkedHashMap<String, String> deviceDetailMap = null;
        try {
            var list = systemParamServiceImpl.getValueByTag("CheckImeiDeviceDetailShowRule");
            checkImeiRequest.setImeiProcessStatus("Invalid");
            if (Arrays.stream(list.split(",")).collect(Collectors.toList()).contains(tag)) {
                checkImeiRequest.setImeiProcessStatus("Valid");
                var gsmaTacDetails = gsmaTacDetailsRepository.getBydeviceId(checkImeiRequest.getImei().substring(0, 8));
                if (gsmaTacDetails == null)
                    logger.info("No MDR detail Found ");
                else
                    deviceDetailMap = deviceDetailsNew(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), checkImeiRequest.getLanguage());
            }
        } catch (Exception e) {
            logger.warn("Not able to get MDR details:" + e.getLocalizedMessage());
        }
        logger.info(" MDR detail Response {}", deviceDetailMap);
        return deviceDetailMap;
    }

    public void saveCheckImeiFailDetails(CheckImeiRequest checkImeiRequest, long startTime, String desc) {
        checkImeiRequest.setRequestProcessStatus("Fail");
        checkImeiRequest.setFail_process_description(desc);
        logger.info(" CHECK_IMEI :Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + ", Response =" + desc);
        new Thread(() -> saveCheckImeiRequest(checkImeiRequest, startTime)).start();//  saveCheckImeiRequest(checkImeiRequest, startTime);
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

    private LinkedHashMap<String, String> deviceDetailsNew(String brand_name, String model_name, String device_type, String manufacturer, String marketing_name, String lang) {
        LinkedHashMap<String, String> item = new LinkedHashMap();
        item.put(chkImeiRespPrmRepo.getByTagAndLanguage("brandName", lang).getValue(), brand_name);
        item.put(chkImeiRespPrmRepo.getByTagAndLanguage("modelName", lang).getValue(), model_name);
        item.put(chkImeiRespPrmRepo.getByTagAndLanguage("manufacturer", lang).getValue(), manufacturer);
        item.put(chkImeiRespPrmRepo.getByTagAndLanguage("marketingName", lang).getValue(), marketing_name);
        item.put(chkImeiRespPrmRepo.getByTagAndLanguage("deviceType", lang).getValue(), device_type);
        return item;
    }

}

// private String getRemarkString(CheckImeiRequest checkImeiRequest, LinkedHashMap<String, Boolean> r) {
//    String remarksValue = "Remarks:";
//    int val = 0;
//    boolean IMEI_PAIRING = r.getOrDefault("IMEI_PAIRING", false);
//    boolean STOLEN = r.getOrDefault(stolenRule.trim(), false);
//    boolean DUPLICATE_DEVICE = r.getOrDefault("DUPLICATE_DEVICE", false);
//    boolean BLACKLIST = r.getOrDefault("EXIST_IN_BLACKLIST_DB", false);
//
//    logger.info("Remarks Check IMEI_PAIRING: {} , Stolen : {} ,DUPLICATE_DEVICE : {} ,BLACKLIST {} ", IMEI_PAIRING, STOLEN, DUPLICATE_DEVICE, BLACKLIST);
//    if (IMEI_PAIRING) {
//        if (DUPLICATE_DEVICE) {
//            val = STOLEN ? 1 : (BLACKLIST ? 2 : 3);
//        } else {
//            val = STOLEN ? 4 : (BLACKLIST ? 5 : 6);
//        }
//    } else {
//        if (DUPLICATE_DEVICE) {
//            val = STOLEN ? 7 : (BLACKLIST ? 8 : 9);
//        } else {
//            val = STOLEN ? 10 : (BLACKLIST ? 11 : 12);
//        }
//    }
//    var remarkTag = "CheckImeiRemark_" + val;
//    logger.info("Remarks tag {} ", remarkTag);
//    var v = chkImeiRespPrmRepo.getByTagAndLanguage(checkImeiRequest.getChannel().equalsIgnoreCase("ussd")
//            ? remarkTag + "ForUssd" : checkImeiRequest.getChannel().equalsIgnoreCase("sms") ?
//            remarkTag + "ForSms" : remarkTag, checkImeiRequest.getLanguage());
//    remarksValue = (v == null || v.getValue().isEmpty())
//            ? " " : v.getValue().replace("<imei>", checkImeiRequest.getImei());
//    remarksValue = remarksValue.substring(0, remarksValue.length() - 1);
//    if (remarksValue.equalsIgnoreCase("Remarks")) {
//        remarksValue = "";
//    }
//    return remarksValue;
//}


//LinkedHashMap mappedDeviceDetails = null;
//var isValidImei = false;
//        try {
//                if (checkImeiRequest.getComplianceValue() == 1 || checkImeiRequest.getComplianceValue() == 4) { // means validity flag 1 ********
//isValidImei = true;
//        logger.info("Going For MDR details ");
//isValidImei = true;
//var gsmaTacDetails = gsmaTacDetailsRepository.getBydeviceId(checkImeiRequest.getImei().substring(0, 8));
//                if (gsmaTacDetails == null)
//        logger.info("No MDR detail Found ");
//                else
//mappedDeviceDetails = deviceDetailsNew(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), checkImeiRequest.getLanguage());
//        }
//        } catch (Exception e) {
//        logger.warn(" **** MDR/NWL Rule might not initialised .");
//        }