/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.exceptions.ResourceServicesException;
import com.gl.ceir.config.model.LanguageLabelDb;
import com.gl.ceir.config.repository.LanguageLabelDbRepository;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gl.ceir.config.model.EnglishLabel;

/**
 *
 * @author maverick
 */
@Service
public class LanguageServiceImpl {

    private static final Logger logger = Logger.getLogger(LanguageServiceImpl.class);

    @Autowired
    LanguageLabelDbRepository languageLabelDbRepository;

    public List<EnglishLabel> getLanguageLabels(String featureName, String language) {
        try {
            return languageLabelDbRepository.getEnglishNameAndLabelFromFeatureName(featureName);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResourceServicesException(this.getClass().getName(), e.getMessage());
        }

    }

}
