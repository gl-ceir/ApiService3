package com.gl.ceir.config.model;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class CheckImeiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private Date createdOn;

    private String imei;
    private Long msisdn;
    private String operator;
    private String imsi;
    private String medium;
    private String language;
    private String interfaces;
    private String requestProcessStatus;
    private String imeiProcessStatus;

}
