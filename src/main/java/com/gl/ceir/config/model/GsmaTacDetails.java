package com.gl.ceir.config.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

// @Table(name = "gsma_tac_db")
@Entity
@Getter
@Setter
public class GsmaTacDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    // Created_on , modified_on
    private String tac;

    private String manufacturer;
    private String modelName;
    private String marketing_name;
    private String brand_name;
    private String allocation_date;
    private String organisation_id;
    private String device_type;
    private String bluetooth;
    private String nfc;
    private String wlan;
    private String removable_uicc;
    private String removable_euicc;

    private String nonremovable_uicc;
    private String nonremovable_euicc;
    private String sim_slot;
    private String imei_quantity;

    private String operating_system;
    private String oem;
    private String action;
    private String network_technology;

}