package com.gl.ceir.config.service.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.gl.ceir.config.configuration.ConnectionConfiguration;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.AppDeviceDetailsDb;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.DeviceidBaseUrlDb;
import com.gl.ceir.config.model.app.GsmaTacDetails;
import com.gl.ceir.config.model.app.Notification;
import com.gl.ceir.config.model.app.RuleEngineMapping;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.repository.app.AuditTrailRepository;
import com.gl.ceir.config.repository.app.CheckImeiRepository;
import com.gl.ceir.config.repository.app.GsmaTacDetailsRepository;
import com.gl.ceir.config.repository.app.SystemConfigurationDbRepository;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.app.AppDeviceDetailsRepository;
import com.gl.ceir.config.repository.app.CheckImeiPreInitRepository;
import com.gl.ceir.config.repository.app.CheckImeiRequestRepository;
import com.gl.ceir.config.repository.app.CheckImeiResponseParamRepository;
import com.gl.ceir.config.repository.app.LanguageLabelDbRepository;
import com.gl.ceir.config.repository.app.NationalWhitelistRepository;
import com.gl.ceir.config.repository.app.OperatorSeriesRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.JSONObject;
//import org.json.JSONObject;
import com.google.gson.Gson;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

@Service
public class CheckImeiServiceImpl {

    private static final Logger logger = LogManager.getLogger(CheckImeiServiceImpl.class);

    @Value("${local-ip}")
    public String localIp;

    @Value("${nullPointerException}")
    private String nullPointerException;

    @Value("${sqlException}")
    private String sQLException;

    @Value("${ruleResponseError}")
    private String ruleResponseError;

    @Value("${someWentWrongException}")
    private String someWentWrongException;

    @Autowired
    CheckImeiRepository checkImeiRepository;

    @Autowired
    AuditTrailRepository auditTrailRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    SystemConfigurationDbRepository systemConfigurationDbRepositry;

    @Autowired
    CheckImeiResponseParamRepository checkImeiResponseParamRepository;

    @Autowired
    GsmaTacDetailsRepository gsmaTacDetailsRepository;

    @Autowired
    ConnectionConfiguration connectionConfiguration;

    @Autowired
    CheckImeiRequestRepository checkImeiRequestRepository;

    @Autowired
    AppDeviceDetailsRepository appDeviceDetailsRepository;

    @Autowired
    LanguageLabelDbRepository languageLabelDbRepository;

    @Autowired
    OperatorSeriesRepository operatorSeriesRepository;

    @Autowired
    CheckImeiPreInitRepository checkImeiPreInitRepository;

    @Autowired
    NationalWhitelistRepository nationalWhitelistRepository;


    /*  *******************************  */

 /*  *******************************  */

 /*  *******************************  */
    public CheckImeiResponse getImeiDetailsDevices(CheckImeiRequest checkImeiRequest, long startTime) {
        //  JSONObject deviceDetails = null;
        String status = null;
        int complianceValue = 0;
        var isValidImei = false;
        GsmaTacDetails gsmaTacDetails = null;
        LinkedHashMap mappedDeviceDetails = null;
        try {
            //   var ruleResponseStatus = getRuleResponse(checkImeiRequest, startTime);
            boolean nationalWhiteListResponse = getnationalWhiteListResponse(checkImeiRequest.getImei().length() > 14 ? checkImeiRequest.getImei().substring(0, 14) : checkImeiRequest.getImei());
            logger.info("Is imei present in natinoal whitelist  :" + nationalWhiteListResponse);
            //   if (ruleResponseStatus.contains("CheckImeiPass")) {
            gsmaTacDetails = gsmaTacDetailsRepository.getBydeviceId(checkImeiRequest.getImei().substring(0, 8));
            if (gsmaTacDetails != null) {
                isValidImei = true;
                //  deviceDetails = deviceDetails(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), checkImeiRequest.getLanguage());
                //  mappedDeviceDetails = new Gson().fromJson(deviceDetails.toString(), LinkedHashMap.class);
                mappedDeviceDetails = deviceDetailsNew(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), checkImeiRequest.getLanguage());

                if (gsmaTacDetails.getDevice_type().equalsIgnoreCase("Smartphone") || gsmaTacDetails.getDevice_type().contains("phone")) {
                    if (nationalWhiteListResponse) {
                        status = "WhiteListedSmartphone";
                        complianceValue = 1;
                    } else {
                        status = "NonWhiteListedSmartphone";
                        complianceValue = 2;
                    }
                } else {
                    if (nationalWhiteListResponse) {
                        status = "WhiteListedOtherDevice";
                        complianceValue = 3;
                    } else {
                        status = "NonWhiteListedOtherDevice";
                        complianceValue = 4;
                    }
                }
                //   } else if (ruleResponseStatus.contains("EXISTS_IN_GSMA_DETAILS_DB")) {
            } else {
                if (nationalWhiteListResponse) {
                    status = "WhiteListedNoDevice";
                    complianceValue = 5;
                } else {
                    status = "NonWhiteListedNoDevice";
                    complianceValue = 6;
                }
            }
            //          else {
//                status = ruleResponseStatus;
//            }
            logger.info("Going for Message Tac Details  :" + gsmaTacDetails + "Status is  :->" + status + "!!! isValidImei" + isValidImei);
            var message = checkImeiResponseParamRepository.getByTagAndTypeAndFeatureName(
                    checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                    ? status + "ForUssd" : status,
                    checkImeiRequest.getLanguage().contains("kh") ? 2 : 1, "CheckImei").getValue()
                    .replace("<imei>", checkImeiRequest.getImei());
            logger.debug("Semi Response  message::  :" + message);
            var compStatus = checkImeiResponseParamRepository.getByTagAndTypeAndFeatureName(
                    checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                    ? status + "ComplianceForUssd" : status + "Compliance",
                    checkImeiRequest.getLanguage().contains("kh") ? 2 : 1, "CheckImei");
            logger.debug("Comp Status:::::::  :" + compStatus);
            var complianceStatus = compStatus == null ? null : compStatus.getValue().replace("<imei>", checkImeiRequest.getImei());;
            logger.debug("Compliance Status::  :" + complianceStatus + ",Response via  mobileDeviceRepository :" + mappedDeviceDetails);
            var symbol_color = systemConfigurationDbRepositry.getByTag(status + "SymbolColor").getValue();    //  message, deviceDetails == null ? null :
            var result = new Result(isValidImei, symbol_color, complianceStatus, message, mappedDeviceDetails == null ? null : mappedDeviceDetails);
            checkImeiRequest.setRequestProcessStatus("Success");
            checkImeiRequest.setImeiProcessStatus(isValidImei == true ? "Valid" : "Invalid");
            checkImeiRequest.setComplianceStatus(complianceStatus);
            checkImeiRequest.setSymbol_color(symbol_color);
            checkImeiRequest.setComplianceValue(complianceValue);
            saveCheckImeiRequest(checkImeiRequest, startTime);
            if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") && systemConfigurationDbRepositry.getByTag("send_sms_flag").getValue().equalsIgnoreCase("true")) {
                logger.info("Going for ussd and send_sms_flag true  ");
                createPostRequestForNotification(checkImeiRequest, result);
            }
            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()), StatusMessage.FOUND.getName(), checkImeiRequest.getLanguage(), result);
        } catch (Exception e) {
            logger.error("Failed at " + e.getLocalizedMessage() + " ----- " + e.toString() + " ::::: " + e.getMessage() + " $$$$$$$$$$$ " + e);
            if (e instanceof NullPointerException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, nullPointerException);
            } else if (e instanceof SQLException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, sQLException);
            } //  else if (e instanceof SQLGrammarException) { saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());            }
            else {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());
            }
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
            logger.error("Failed at " + e.getLocalizedMessage());
            saveCheckImeiRequest(checkImeiRequest, startTime);
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    public void saveCheckImeiFailDetails(CheckImeiRequest checkImeiRequest, long startTime, String desc) {
        checkImeiRequest.setRequestProcessStatus("Fail");
        logger.warn("------------------------------------------------------- " + desc);
        checkImeiRequest.setFail_process_description(desc);
        // alertServiceImpl.raiseAnAlert(Alerts.ALERT_1110.getName(), desc, "Check Imei ", 0);
        logger.info(" CHECK_IMEI :  Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + " ########## Response =" + desc);
        saveCheckImeiRequest(checkImeiRequest, startTime);
    }

    public void saveCheckImeiRequest(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            checkImeiRequest.setCheckProcessTime(String.valueOf(System.currentTimeMillis() - startTime));
            checkImeiRequestRepository.save(checkImeiRequest);
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1110.getName(), 0);
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    public String globalErrorMsgs(String language) {
        return checkImeiResponseParamRepository.getByTagAndTypeAndFeatureName("CheckImeiErrorMessage",
                language.contains("kh") ? 2 : 1, "CheckImei").getValue();
    }

    private void createPostRequestForNotification(CheckImeiRequest checkImeiRequest, Result result) {
        var notification = new Notification("SMS", result.getMessage(), "CheckImei", 0, 0, checkImeiRequest.getMsisdn(),
                checkImeiRequest.getOperator(), checkImeiRequest.getLanguage(), checkImeiRequest.getOperator());
        Gson gson = new Gson();
        String body = gson.toJson(notification, Notification.class);
        sendPostForSmsNotification(body);
    }

    private String sendPostForSmsNotification(String body) {
        String url = systemConfigurationDbRepositry.getByTag("notificationTableUrl")
                .getValue()
                .replace("{localIp}", localIp);
        StringBuffer response = new StringBuffer();
        logger.info("POST  Start Url-> " + url + " ;Body->" + body);
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");
            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.flush();
            os.close();
            // For POST only - END
            int responseCode = con.getResponseCode();
            logger.info("POST Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                logger.info(response.toString());
            } else {
                logger.warn("POST request not worked");
                alertServiceImpl.raiseAnAlert(Alerts.ALERT_1107.getName(), 0);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return response.toString();
    }

    private boolean getnationalWhiteListResponse(String imei) {
        logger.debug("NationalWhiteListResponse:" + nationalWhitelistRepository.getByImei(imei));
        return nationalWhitelistRepository.getByImei(imei) == null ? false : true;
    }


    public DeviceidBaseUrlDb getPreinitApi(String deviceId) {
        try {
            var response = checkImeiPreInitRepository.getByDeviceId(deviceId);
            if (response == null) {
                response = checkImeiPreInitRepository.getByDeviceId("default_setup");
            }
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage() + " : " + e.getLocalizedMessage());
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1106.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), e.getLocalizedMessage());
        }
    }

    public void saveDeviceDetails(AppDeviceDetailsDb appDeviceDetailsDb) {
        try {
            appDeviceDetailsRepository.saveDetails(
                    appDeviceDetailsDb.getOsType(),
                    appDeviceDetailsDb.getDeviceId(),
                    appDeviceDetailsDb.getDeviceDetails().toJSONString(),
                    appDeviceDetailsDb.getLanguageType());
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1104.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), "internal server error");
        }
    }

    private LinkedHashMap deviceDetailsNew(String brand_name, String model_name, String device_type, String manufacturer, String marketing_name, String lang) {
        LinkedHashMap<String, String> item = new LinkedHashMap();
        item.put(lang.equals("en") ? "Brand Name" : languageLabelDbRepository.getKhmerNameFromLabel("brandName"), brand_name);
        item.put(lang.equals("en") ? "Model Name" : languageLabelDbRepository.getKhmerNameFromLabel("modelName"), model_name);
        item.put(lang.equals("en") ? "Manufacturer" : languageLabelDbRepository.getKhmerNameFromLabel("manufacturer"), manufacturer);
        item.put(lang.equals("en") ? "Marketing Name" : languageLabelDbRepository.getKhmerNameFromLabel("marketingName"), marketing_name);
        item.put(lang.equals("en") ? "Device Type" : languageLabelDbRepository.getKhmerNameFromLabel("deviceType"), device_type);
        return item;
    }
}

//      private JSONObject deviceDetails(String brand_name, String modelName, String device_type, String manufacturer, String marketing_name, String lang) {
//        JSONObject item = new JSONObject();
//        try {
//            Field map = item.getClass().getDeclaredField("map");
//            map.setAccessible(true);//because the field is private final...
//            map.set(item, new LinkedHashMap<>());
//            map.setAccessible(false);//return flag
//        } catch (Exception e) {
//            logger.error("Json serial at " + e.getLocalizedMessage() + " ie " + e.getMessage());
//        }
//        item.put(lang.equals("en") ? "Brand Name" : languageLabelDbRepository.getKhmerNameFromLabel("brandName"), brand_name);
//        item.put(lang.equals("en") ? "Model Name" : languageLabelDbRepository.getKhmerNameFromLabel("modelName"), modelName);
//        item.put(lang.equals("en") ? "Manufacturer" : languageLabelDbRepository.getKhmerNameFromLabel("manufacturer"), manufacturer);
//        item.put(lang.equals("en") ? "Marketing Name" : languageLabelDbRepository.getKhmerNameFromLabel("marketingName"), marketing_name);
//        item.put(lang.equals("en") ? "Device Type" : languageLabelDbRepository.getKhmerNameFromLabel("deviceType"), device_type);
//        return item;
//    }

//    private String getRuleResponse(CheckImeiRequest checkImeiRequest, long startTime) {
//        var ruleResponseStatus
//                = checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
//                ? "CheckImeiPassForUssd" : "CheckImeiPass";
//        try (Connection conn = connectionConfiguration.getConnection()) {
//            List<RuleEngineMapping> ruleList = checkImeiRepository.getByFeatureAndUserTypeOrderByRuleOrder("CheckImei", "default");
//            for (RuleEngineMapping rules : ruleList) {
//                Rule rule = new Rule(rules.getName(), rules.getOutput(), rules.getRuleMessage());
//                String[] my_arr = {
//                    rule.rule_name, "1", "NONCDR",
//                    checkImeiRequest.getImei(),
//                    "", "", "", "", "", "IMEI", "", " ", " ", ""};
//                String expOutput = RuleEngineApplication.startRuleEngine(my_arr, conn, null);
//                if (!rule.output.equalsIgnoreCase(expOutput)) {
//                    ruleResponseStatus = rule.rule_name;
//                    break;
//                }
//            }
//            conn.close();
//            return ruleResponseStatus;
//        } catch (SQLException e) {
//            logger.error(e.getMessage() + " : " + e.getLocalizedMessage());
//            saveCheckImeiFailDetails(checkImeiRequest, startTime, ruleResponseError);
//            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
//        }
//    }
