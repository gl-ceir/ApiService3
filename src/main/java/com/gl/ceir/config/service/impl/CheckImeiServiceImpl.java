package com.gl.ceir.config.service.impl;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gl.Rule_engine.RuleEngineApplication;
import com.gl.ceir.config.configuration.ConnectionConfiguration;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.exceptions.UnprocessableEntityException;

import com.gl.ceir.config.exceptions.ResourceServicesException;
import com.gl.ceir.config.model.AppDeviceDetailsDb;
import com.gl.ceir.config.model.CheckImeiRequest;
import com.gl.ceir.config.model.Result;
import com.gl.ceir.config.model.CheckImeiResponse;
import com.gl.ceir.config.model.DeviceDetails;

import com.gl.ceir.config.model.RuleEngineMapping;
import com.gl.ceir.config.model.SystemConfigurationDb;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.repository.AuditTrailRepository;
import com.gl.ceir.config.repository.CheckImeiRepository;
import com.gl.ceir.config.repository.GsmaTacDetailsRepository;
import com.gl.ceir.config.repository.SystemConfigurationDbRepository;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.AppDeviceDetailsRepository;
import com.gl.ceir.config.repository.CheckImeiRequestRepository;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
//import org.json.JSONObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;

@Service
public class CheckImeiServiceImpl {

    private static final Logger logger = Logger.getLogger(CheckImeiServiceImpl.class);

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
            List<RuleEngineMapping> ruleList = checkImeiRepository.getByFeatureAndUserTypeOrderByRuleOrder(feature,
                    user_type);
            logger.info("RuleList is " + ruleList);
            for (RuleEngineMapping cim : ruleList) {
                Rule rule = new Rule(cim.getName(), cim.getOutput(), cim.getRuleMessage());
                // rule_details.add(rule);
                // }
                // logger.info("Rules Populated"); // optimse
                // for (Rule rule : rule_details) {

                String[] my_arr = {rule.rule_name, "1", "NONCDR",
                    ((rule.rule_name.equals("IMEI_LUHN_CHECK") || rule.rule_name.equals("IMEI_LENGTH")) ? imei
                    : imei.substring(0, 14)),
                    "4", "5", "6", "7", "8", deviceIdValue, "", " ", " ", ""};
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
        StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml")
                .build();

        Metadata metadata = new MetadataSources(standardRegistry)
                .addAnnotatedClass(CheckImeiServiceImpl.class)
                .buildMetadata();

        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder()
                .build();

        try {
            c1 = sessionFactory.getSessionFactoryOptions().getServiceRegistry().getService(ConnectionProvider.class)
                    .getConnection();
            logger.info("Connection for Rule " + c1);
            logger.info(c1.getMetaData().getDatabaseProductName());

        } catch (Exception e) {
            logger.info(" Erorr " + e);
        }
        return c1;

    }

    public Object getImeiDetailsDevices(CheckImeiRequest checkImeiRequest) {
        JSONObject deviceDetails = null;
        var isValidImei = false;
        var ruleResponseStatus = checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                ? "CheckImeiPassForUssd" : "CheckImeiPass";

        var language = checkImeiRequest.getLanguage() == null ? "en" : checkImeiRequest.getLanguage();
        try (Connection conn = connectionConfiguration.getConnection()) {
            new Thread(() -> {
                checkImeiRequestRepository.save(checkImeiRequest);
            }).start();
            List<RuleEngineMapping> ruleList = checkImeiRepository.getByFeatureAndUserTypeOrderByRuleOrder("CheckImei",
                    "default");
            for (RuleEngineMapping rules : ruleList) {
                Rule rule = new Rule(rules.getName(), rules.getOutput(), rules.getRuleMessage());
                String[] my_arr = {rule.rule_name, "1", "NONCDR",
                    (rule.rule_name.equals("IMEI_LENGTH") ? checkImeiRequest.getImei()
                    : checkImeiRequest.getImei()
                    .substring(0, 14)), "", "", "", "", "", "IMEI", "", " ", " ", ""};
                String expOutput = RuleEngineApplication.startRuleEngine(my_arr, conn, null);
                if (!rule.output.equalsIgnoreCase(expOutput)) {
                    ruleResponseStatus = rule.rule_name;
                    break;
                }
            }
            logger.debug("Rule Status :" + ruleResponseStatus);
            SystemConfigurationDb systemConfigurationDb = systemConfigurationDbRepository
                    .getByTagAndTypeAndFeatureName(ruleResponseStatus, language.contains("kh") ? 2 : 1, "CheckImei");
            var message = systemConfigurationDb.getValue().replace("$imei", checkImeiRequest.getImei());
            logger.debug("Message :" + systemConfigurationDb.getValue());
            if (ruleResponseStatus.contains("CheckImeiPass")) {
                isValidImei = true;
                var gsmaTacDetails = gsmaTacDetailsRepository
                        .getByTac(checkImeiRequest.getImei().substring(0, 8));
                message = message
                        .replace("$brandName", gsmaTacDetails.getBrand_name())
                        .replace("$modelName", gsmaTacDetails.getModelName())
                        .replace("$deviceType", gsmaTacDetails.getDevice_type())
                        .replace("$manufacturer", gsmaTacDetails.getDevice_type())
                        .replace("$marketingName", gsmaTacDetails.getMarketing_name());
                deviceDetails = deviceDetails(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModelName(), gsmaTacDetails.getDevice_type(),
                        gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name());
            }
            logger.info("Response :" + deviceDetails);
            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()),
                    StatusMessage.FOUND.getName(), language.contains("kh") ? "kh" : "en",
                    new Result(isValidImei, message, deviceDetails));
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
            logger.error("Failed at " + e.getLocalizedMessage());
            throw new InternalServicesException(this.getClass().getName(), "internal server error");
        }
    }

    public void saveDeviceDetails(AppDeviceDetailsDb appDeviceDetailsDb) {
        try {
            appDeviceDetailsRepository.saveDetails(appDeviceDetailsDb.getOsType(), appDeviceDetailsDb.getDeviceId(), appDeviceDetailsDb.getDeviceDetails().toJSONString());
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1104.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), "Something went wrong");
        }
    }

    private JSONObject deviceDetails(String brand_name, String modelName, String device_type, String manufacturer, String marketing_name) {
        JSONObject item = new JSONObject();  // check with  response = String.class)
        item.put("Brand Name", brand_name);
        item.put("Model Name", modelName);
        item.put("Manufacturer", manufacturer);
        item.put("Marketing Name", marketing_name);
        item.put("Device Type", device_type);
        return item;
    }
}
