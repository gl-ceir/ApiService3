package com.gl.ceir.config.model.app;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "check_imei_req_detail")

public class CheckImeiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private Date createdOn;

    private String imei;
    private String msisdn;
    private String operator;
    private String imsi;
    private String language;
    private String channel;
    private String requestProcessStatus ;
    private String imeiProcessStatus;
    private String checkProcessTime;
    private String complianceStatus;
    private String utm_source;
    private String browser;
    private String public_ip;
    private String header_browser;
    private String header_public_ip;
    private String symbol_color;

    @Override
    public String toString() {
        return "CheckImeiRequest{" + "id=" + id + ", createdOn=" + createdOn + ", imei=" + imei + ", msisdn=" + msisdn + ", operator=" + operator + ", imsi=" + imsi + ", language=" + language + ", channel=" + channel + ", requestProcessStatus=" + requestProcessStatus + ", imeiProcessStatus=" + imeiProcessStatus + ", checkProcessTime=" + checkProcessTime + ", complianceStatus=" + complianceStatus + ", utm_source=" + utm_source + ", browser=" + browser + ", public_ip=" + public_ip + ", header_browser=" + header_browser + ", header_public_ip=" + header_public_ip + ", symbol_color=" + symbol_color + '}';
    }

}
