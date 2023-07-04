package com.gl.ceir.config.service.impl;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.gl.ceir.config.configuration.ConnectionConfiguration;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.exceptions.ResourceServicesException;
import com.gl.ceir.config.model.app.AppDeviceDetailsDb;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.DeviceidBaseUrlDb;
import com.gl.ceir.config.model.app.GsmaTacDetails;
import com.gl.ceir.config.model.app.Notification;
import com.gl.ceir.config.model.app.RuleEngineMapping;
import com.gl.ceir.config.model.app.SystemConfigurationDb;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.repository.app.AuditTrailRepository;
import com.gl.ceir.config.repository.app.CheckImeiRepository;
import com.gl.ceir.config.repository.app.GsmaTacDetailsRepository;
import com.gl.ceir.config.repository.app.SystemConfigurationDbRepository;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.app.AppDeviceDetailsRepository;
import com.gl.ceir.config.repository.app.CheckImeiPreInitRepository;
import com.gl.ceir.config.repository.app.CheckImeiRequestRepository;
import com.gl.ceir.config.repository.app.LanguageLabelDbRepository;
import com.gl.ceir.config.repository.app.NationalWhitelistRepository;
import com.gl.ceir.config.repository.app.OperatorSeriesRepository;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

@Service
public class CheckImeiServiceImpl {

    private static final Logger logger = LogManager.getLogger(CheckImeiServiceImpl.class);

    @Autowired
    CheckImeiRepository checkImeiRepository;

    @Autowired
    AuditTrailRepository auditTrailRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    SystemConfigurationDbRepository systemConfigurationDbRepository;

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

    @Value("${local-ip}")
    public String localIp;

    public String getResult(String user_type, String feature, String imei, Long imei_type) {
        String rulePass = "true";

        try {
            Connection conn = getSQlConnection();
            BufferedWriter bw = null;
            String expOutput = "";
            // ArrayList<Rule> rule_details = new ArrayList<Rule>();
            int i = imei_type.intValue();
            String deviceIdValue = null;
            switch (i) {
                case 0:
                    deviceIdValue = "IMEI";
                    break;
                case 1:
                    deviceIdValue = "MEID";
                    break;
                case 2:
                    deviceIdValue = "ESN";
                    break;
            }
            // deviceIdValue = "IMEI"; // to be remove if another values came ,
            List<RuleEngineMapping> ruleList
                    = checkImeiRepository.getByFeatureAndUserTypeOrderByRuleOrder(feature, user_type);
            logger.info("RuleList is " + ruleList);
            for (RuleEngineMapping cim : ruleList) {
                Rule rule = new Rule(cim.getName(), cim.getOutput(), cim.getRuleMessage());
                // rule_details.add(rule);
                // }
                // logger.info("Rules Populated"); // optimse
                // for (Rule rule : rule_details) {

                String[] my_arr = {
                    rule.rule_name,
                    "1",
                    "NONCDR",
                    ((rule.rule_name.equals("IMEI_LUHN_CHECK") || rule.rule_name.equals("IMEI_LENGTH"))
                    ? imei
                    : imei.substring(0, 14)),
                    "4",
                    "5",
                    "6",
                    "7",
                    "8",
                    deviceIdValue,
                    "",
                    " ",
                    " ",
                    ""
                };
                logger.info("Rule : " + rule.rule_name);
                expOutput = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);
                logger.info("Rule Output By Engine :" + expOutput + " , Expected Output : " + rule.output);
                if (rule.output.equalsIgnoreCase(expOutput)) { // go to next rule( rule_engine _mapping )
                    logger.info("Rule Passed");
                } else {
                    logger.info("Rule failed at " + rule.rule_name);
                    rulePass = rule.rule_name;
                    break;
                }
            }
            logger.info("Conn Close");
            conn.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResourceServicesException(this.getClass().getName(), e.getMessage());
        } finally {

            // auditTrailRepository.save(new AuditTrail(checkImeiValuesEntity.getUserId(),
            // checkImeiValuesEntity.getUserName(),
            // Long.valueOf(checkImeiValuesEntity.getUserTypeId()),
            // checkImeiValuesEntity.getUserType(),
            // Long.valueOf(checkImeiValuesEntity.getFeatureId()), Features.CONSIGNMENT,
            // "VIEW_ALL", "", "NA",
            // checkImeiValuesEntity.getRoleType(), checkImeiValuesEntity.getPublicIp(),
            // checkImeiValuesEntity.getBrowser()));
            // logger.info("AUDIT : Saved view request in audit.");
        }
        return rulePass;
    }

    private Connection getSQlConnection() throws SQLException {
        Connection c1 = null;
        StandardServiceRegistry standardRegistry
                = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();

        Metadata metadata
                = new MetadataSources(standardRegistry)
                        .addAnnotatedClass(CheckImeiServiceImpl.class)
                        .buildMetadata();

        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

        try {
            c1
                    = sessionFactory
                            .getSessionFactoryOptions()
                            .getServiceRegistry()
                            .getService(ConnectionProvider.class)
                            .getConnection();
            logger.info("Connection for Rule " + c1);
            logger.info(c1.getMetaData().getDatabaseProductName());

        } catch (Exception e) {
            logger.info(" Erorr " + e);
        }
        return c1;
    }

    public CheckImeiResponse getImeiDetailsDevices(CheckImeiRequest checkImeiRequest) {
        JSONObject deviceDetails = null;
        String status = null;
        var isValidImei = false;
        GsmaTacDetails gsmaTacDetails = null;
        LinkedHashMap mappedDeviceDetails = null;
        var startTime = System.currentTimeMillis();
        logger.info("Start Time =" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        try {
            var language = checkImeiRequest.getLanguage() == null ? "en" : checkImeiRequest.getLanguage();
            var ruleResponseStatus = getRuleResponse(checkImeiRequest);
            logger.info("Rule Response Status :" + ruleResponseStatus);
            boolean nationalWhiteListResponse = getnationalWhiteListResponse(checkImeiRequest.getImei().substring(0, 14));
            logger.info("Is imei present in natinoal whitelist  :" + nationalWhiteListResponse);
            if (ruleResponseStatus.contains("CheckImeiPass")) {
                isValidImei = true;
                gsmaTacDetails = gsmaTacDetailsRepository.getBydeviceId(checkImeiRequest.getImei().substring(0, 8));
                deviceDetails = deviceDetails(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), language);
                mappedDeviceDetails = new Gson().fromJson(deviceDetails.toString(), LinkedHashMap.class);
                logger.info("Going for Message Tac Details  :" + gsmaTacDetails);
                if (gsmaTacDetails.getDevice_type().equalsIgnoreCase("Smartphone") || gsmaTacDetails.getDevice_type().contains("phone")) {
                    if (nationalWhiteListResponse) {
                        status = "WhiteListedSmartphone";
                    } else {
                        status = "NonWhiteListedSmartphone";
                    }
                } else {
                    if (nationalWhiteListResponse) {
                        status = "WhiteListedOtherDevice";
                    } else {
                        status = "NonWhiteListedOtherDevice";
                    }
                }
            } else if (ruleResponseStatus.contains("EXISTS_IN_GSMA_DETAILS_DB")) {
                if (nationalWhiteListResponse) {
                    status = "WhiteListedNoDevice";
                } else {
                    status = "NonWhiteListedNoDevice";
                }
            } else {
                status = ruleResponseStatus;
            }

            logger.info("Status is  :->" + status);
            var message = systemConfigurationDbRepository.getByTagAndTypeAndFeatureName(
                    checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                    ? status + "ForUssd" : status,
                    language.contains("kh") ? 2 : 1, "CheckImei").getValue()
                    .replace("$imei", checkImeiRequest.getImei());

//            if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")) {
//                message = message.replace("$brandName", gsmaTacDetails.getBrand_name())
//                        .replace("$modelName", gsmaTacDetails.getModel_name())
//                        .replace("$deviceType", gsmaTacDetails.getDevice_type())
//                        .replace("$manufacturer", gsmaTacDetails.getManufacturer())
//                        .replace("$marketingName", gsmaTacDetails.getMarketing_name());
//            }
            logger.info("Semi Response  message::  :" + message);
            var compStatus = systemConfigurationDbRepository.getByTagAndTypeAndFeatureName(
                    checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                    ? status + "ComplianceForUssd" : status + "Compliance",
                    language.contains("kh") ? 2 : 1, "CheckImei");
            logger.info("Compliance Status:::::::  :" + compStatus);

            var complianceStatus = compStatus == null ? null : compStatus.getValue();
            logger.info("Compliance Status::  :" + complianceStatus);
            logger.info("Response via  mobileDeviceRepository :" + mappedDeviceDetails);
            var result = new Result(isValidImei, complianceStatus, message, deviceDetails == null ? null : mappedDeviceDetails);
            checkImeiRequest.setRequestProcessStatus("Success");
            checkImeiRequest.setImeiProcessStatus(isValidImei == true ? "Valid" : "Invalid");
            checkImeiRequest.setComplianceStatus(complianceStatus);
            saveCheckImeiRequest(checkImeiRequest, startTime);
            if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") && systemConfigurationDbRepository.getByTag("send_sms_flag").getValue().equalsIgnoreCase("true")) {
                logger.info("Going for ussd and send_sms_flag true  ");
                createPostRequestForNotification(checkImeiRequest, result);
            }
            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()), StatusMessage.FOUND.getName(), language.contains("kh") ? "kh" : "en", result);
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
            logger.error("Failed at " + e.getLocalizedMessage());
            checkImeiRequest.setRequestProcessStatus("Fail");
            saveCheckImeiRequest(checkImeiRequest, startTime);
            throw new InternalServicesException(this.getClass().getName(), "internal server error");
        }
    }

    private void saveCheckImeiRequest(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            checkImeiRequest.setCheckProcessTime(
                    String.valueOf(System.currentTimeMillis() - startTime));
            checkImeiRequestRepository.save(checkImeiRequest);
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1104.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), "internal server error");
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

    private JSONObject deviceDetails(String brand_name, String modelName, String device_type, String manufacturer, String marketing_name, String lang) {

        JSONObject item = new JSONObject();
        try {
            Field map = item.getClass().getDeclaredField("map");
            map.setAccessible(true);//because the field is private final...
            map.set(item, new LinkedHashMap<>());
            map.setAccessible(false);//return flag
        } catch (Exception e) {
            logger.error("Json serial at " + e.getLocalizedMessage() + " ie " + e.getMessage());
        }
        item.put(lang.equals("en") ? "Brand Name" : languageLabelDbRepository.getKhmerNameFromLabel("brandName"), brand_name);
        item.put(lang.equals("en") ? "Model Name" : languageLabelDbRepository.getKhmerNameFromLabel("modelName"), modelName);
        item.put(lang.equals("en") ? "Manufacturer" : languageLabelDbRepository.getKhmerNameFromLabel("manufacturer"), manufacturer);
        item.put(lang.equals("en") ? "Marketing Name" : languageLabelDbRepository.getKhmerNameFromLabel("marketingName"), marketing_name);
        item.put(lang.equals("en") ? "Device Type" : languageLabelDbRepository.getKhmerNameFromLabel("deviceType"), device_type);
        return item;
    }

    private void createPostRequestForNotification(CheckImeiRequest checkImeiRequest, Result result) {
        var notification = new Notification("SMS", result.getMessage(), "CheckImei", 0, 0, checkImeiRequest.getMsisdn(),
                checkImeiRequest.getOperator(), checkImeiRequest.getLanguage(), checkImeiRequest.getOperator());
        Gson gson = new Gson();
        String body = gson.toJson(notification, Notification.class);
        sendPostForSmsNotification(body);
    }

    private String sendPostForSmsNotification(String body) {
        String url = systemConfigurationDbRepository.getByTag("notificationTableUrl")
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
                logger.info("POST request not worked");
                alertServiceImpl.raiseAnAlert(Alerts.ALERT_1107.getName(), 0);

            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return response.toString();
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
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1105.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), e.getLocalizedMessage());
        }
    }

    private boolean getnationalWhiteListResponse(String imei) {
        logger.info("NationalWhiteListResponse:" + nationalWhitelistRepository.getByImei(imei));
        return nationalWhitelistRepository.getByImei(imei) == null ? false : true;
    }

    private String getRuleResponse(CheckImeiRequest checkImeiRequest) {
        var ruleResponseStatus
                = checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                ? "CheckImeiPassForUssd" : "CheckImeiPass";
        try (Connection conn = connectionConfiguration.getConnection()) {
            List<RuleEngineMapping> ruleList = checkImeiRepository.getByFeatureAndUserTypeOrderByRuleOrder("CheckImei", "default");
            for (RuleEngineMapping rules : ruleList) {
                Rule rule = new Rule(rules.getName(), rules.getOutput(), rules.getRuleMessage());
                String[] my_arr = {
                    rule.rule_name, "1", "NONCDR", (rule.rule_name.equals("IMEI_LENGTH")
                    ? checkImeiRequest.getImei() : checkImeiRequest.getImei().substring(0, 14)), "", "", "", "", "", "IMEI", "", " ", " ", ""};
                String expOutput = RuleEngineApplication.startRuleEngine(my_arr, conn, null);
                if (!rule.output.equalsIgnoreCase(expOutput)) {
                    ruleResponseStatus = rule.rule_name;
                    break;
                }
            }
            return ruleResponseStatus;
        } catch (SQLException e) {
            logger.error(e.getMessage() + " : " + e.getLocalizedMessage());
            throw new InternalServicesException(this.getClass().getName(), e.getLocalizedMessage());
        }

    }

}
