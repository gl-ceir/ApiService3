/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.simple.JSONObject;
import javax.persistence.Transient;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class AppDeviceDetailsDb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String languageType;
    private String deviceId;
    private String osType;
    private JSONObject deviceDetails;

    public AppDeviceDetailsDb(String languageType, String deviceId, String osType, JSONObject deviceDetails) {
        this.languageType = languageType;
        this.deviceId = deviceId;
        this.osType = osType;
        this.deviceDetails = deviceDetails;
    }

}
