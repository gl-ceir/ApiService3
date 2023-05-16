/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.controller;

import com.gl.ceir.config.exceptions.UnprocessableEntityException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.gl.ceir.config.service.impl.LanguageServiceImpl;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class LanguageController {

    private static final Logger logger = LogManager.getLogger(LanguageController.class);

    @Autowired
    LanguageServiceImpl languageServiceImpl;

    @ApiOperation(value = "Get All Labels as Per Language", response = String.class)
    @RequestMapping(path = "dialectRetreiver", method = RequestMethod.GET)
    public MappingJacksonValue getLanguageLabels(@RequestParam("language") String language,
            @RequestParam("feature_name") String feature_name) {
        MappingJacksonValue mapping = new MappingJacksonValue(languageServiceImpl.getLanguageLabels(feature_name, language));
        logger.info("Response of View =" + mapping);
        return mapping;
    }

}
