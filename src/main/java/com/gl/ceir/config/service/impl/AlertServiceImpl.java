/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.model.app.AlertDb;
import com.gl.ceir.config.model.app.RunningAlertDb;
import com.gl.ceir.config.repository.app.AlertDbRepository;
import com.gl.ceir.config.repository.app.RunningAlertDbRepository;

import com.gl.ceir.config.model.app.GenricResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl {

    private static final Logger logger = LogManager.getLogger(AlertServiceImpl.class);

    @Autowired
    AlertDbRepository alertDbRepository;

    @Autowired
    RunningAlertDbRepository runningAlertDbRepository;

    public GenricResponse raiseAnAlert(String alertId, int userId) {
        try {
            AlertDb alertDb = alertDbRepository.getByAlertId(alertId);
            runningAlertDbRepository.save(new RunningAlertDb(userId, alertId, alertDb.getDescription(), 0));
            return new GenricResponse(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new GenricResponse(1);
        }
    }

    public void raiseAnAlert1(String alertCode, String alertMessage, String alertProcess, int userId) {
        try {
            logger.info("Alert  " + alertMessage);
            String path = System.getenv("APP_HOME") + "alert/start.sh";
            ProcessBuilder pb = new ProcessBuilder(path, alertCode, alertMessage, alertProcess, String.valueOf(userId));
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            String response = null;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            logger.info("Alert is generated ");
        } catch (Exception ex) {
            logger.error("Not able to execute Alert mgnt jar ", ex.getLocalizedMessage() + " ::: " + ex.getMessage());
        }
    }

//    public static void callAlertMgmt(String alertCode, String alertMessage, String alertProcess, String userId) {
//        try {
//            String alertJar = System.getenv("APP_HOME") + "/alert/start.sh";
//            Process upload = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "\"" + alertJar + "\"" + "\"" + alertCode + "\"" + "\"" + alertMessage + "\"" + "\"" + alertProcess + "\"" + "\"" + userId + "\""});
//            upload.waitFor();
//            if (upload.exitValue() == 0) {
//                logger.info("Process successfully executed ");
//            }            //      return new GenricResponse(0);

//        } catch (Exception e) { //      return new GenricResponse(1);
//            logger.error("Not able to execute Alert mgnt jar ", e);
//        }
//    }


}

//        try { 
//            AlertDb alertDb = alertDbRepository.getByAlertId(alertId);
//            // Replace Placeholders from bodyPlaceHolderMap.
//            if (Objects.nonNull(bodyPlaceHolderMap)) {
//                for (Map.Entry<String, String> entry : bodyPlaceHolderMap.entrySet()) {
//                    logger.info("Placeholder key : " + entry.getKey() + " value : " + entry.getValue());
//                    alertDb.setDescription(alertDb.getDescription().replaceAll(entry.getKey(), entry.getValue()));
//                }
//            }
//            runningAlertDbRepository.save(new RunningAlertDb(userId, alertId, alertDb.getDescription(), 0));
//            return new GenricResponse(0);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return new GenricResponse(1);
//        }
