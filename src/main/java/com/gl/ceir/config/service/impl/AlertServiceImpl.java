/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.model.AlertDb;
import com.gl.ceir.config.model.RunningAlertDb;
import com.gl.ceir.config.repository.AlertDbRepository;
import com.gl.ceir.config.repository.RunningAlertDbRepository;

import com.gl.ceir.config.model.GenricResponse;
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

    public GenricResponse raiseAnAlert(String alertId, int userId, Map<String, String> bodyPlaceHolderMap) {

        try {
            AlertDb alertDb = alertDbRepository.getByAlertId(alertId);

            // Replace Placeholders from bodyPlaceHolderMap.
            if (Objects.nonNull(bodyPlaceHolderMap)) {
                for (Map.Entry<String, String> entry : bodyPlaceHolderMap.entrySet()) {
                    logger.info("Placeholder key : " + entry.getKey() + " value : " + entry.getValue());
                    alertDb.setDescription(alertDb.getDescription().replaceAll(entry.getKey(), entry.getValue()));
                }
            }

            runningAlertDbRepository.save(new RunningAlertDb(userId, alertId, alertDb.getDescription(), 0));
            return new GenricResponse(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new GenricResponse(1);
        }
    }

}