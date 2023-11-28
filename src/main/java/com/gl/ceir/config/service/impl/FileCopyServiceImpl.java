/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.model.app.AlertDb;
import com.gl.ceir.config.model.app.AlertRequest;
import com.gl.ceir.config.model.app.GenricResponse;
import com.gl.ceir.config.model.app.RunningAlertDb;
import com.gl.ceir.config.model.app.UploadedFileDB;
import com.gl.ceir.config.repository.app.AlertDbRepository;
import com.gl.ceir.config.repository.app.RunningAlertDbRepository;
import com.gl.ceir.config.repository.app.UploadedFileDBRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileCopyServiceImpl {

    private static final Logger logger = LogManager.getLogger(FileCopyServiceImpl.class);

    @Autowired
    UploadedFileDBRepository uploadedFileDBRepository;

    public GenricResponse saveDetailsWithParam(UploadedFileDB uploadedFileDB) {
        try {
            var id = uploadedFileDBRepository.save(uploadedFileDB);
            return new GenricResponse(0, "Success", String.valueOf(id.getId()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new GenricResponse(1, "Fail", e.getLocalizedMessage());
        }
    }

}
