/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.repository.LanguageLabelDbRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gl.ceir.config.model.LanguageResponse;
import com.gl.ceir.config.model.constants.Alerts;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author maverick
 */
@Service
public class LanguageServiceImpl {

    private static final Logger logger = Logger.getLogger(LanguageServiceImpl.class);

    @Autowired
    LanguageLabelDbRepository languageLabelDbRepository;

    @Autowired
    AlertServiceImpl alertServiceImpl;

    public LanguageResponse getLanguageLabels(String featureName, String language) {
        String responseValue;
        try {
            if (language.contains("kh")) {
                responseValue = languageLabelDbRepository.getKhmerNameAndLabelFromFeatureName(featureName);
            } else {
                responseValue = languageLabelDbRepository.getEnglishNameAndLabelFromFeatureName(featureName);
            }
            return new LanguageResponse(language, (JSONObject) new JSONParser().parse(responseValue));
        } catch (Exception e) {
            logger.error(e.getMessage() +" : "+ e.getLocalizedMessage());
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1105.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), e.getLocalizedMessage());
        }
    }
}
