/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.controller;

import com.gl.ceir.config.service.impl.AlertServiceImpl;

import com.gl.ceir.config.service.impl.FileCopyServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gl.ceir.config.model.app.AlertRequest;
import com.gl.ceir.config.model.app.UploadedFileDB;

@RestController
public class AlertController {

    @Autowired
    private AlertServiceImpl alertServiceImpl;
    
    
    @Autowired
    private FileCopyServiceImpl fileCopyServiceImpl;
    

    @RequestMapping(path = "/alert/{id}", method = RequestMethod.GET)
    public MappingJacksonValue raiseAlertById(@PathVariable(value = "id") String id) {
        var response = alertServiceImpl.raiseAlertById(id);
        MappingJacksonValue mapping = new MappingJacksonValue(response);
        return mapping;
    }

    @PostMapping("/alert")
    public MappingJacksonValue save(@RequestBody AlertRequest alertRequest) {
        var action = alertServiceImpl.saveAlertWithParam(alertRequest);
        MappingJacksonValue mapping = new MappingJacksonValue(action);
        return mapping;
    }
    
        @PostMapping("/addFileToSync")
    public MappingJacksonValue saveFileCopyDetails(@RequestBody UploadedFileDB uploadedFileDB) {
        var action = fileCopyServiceImpl.saveDetailsWithParam(uploadedFileDB);
        MappingJacksonValue mapping = new MappingJacksonValue(action);
        return mapping;
    }
    
}
