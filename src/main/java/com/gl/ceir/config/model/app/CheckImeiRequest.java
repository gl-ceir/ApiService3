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
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "check_imei_req_detail")
@DynamicInsert
public class CheckImeiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imei;
    private String msisdn;
    private String operator;
    private String imsi;
    private String language;
    private String channel;
    private String utm_source;
    private String browser;
    private String public_ip;
    private String header_browser;
    private String header_public_ip;
    private String os_type;

    private String requestProcessStatus;
    private String imeiProcessStatus;
    private String checkProcessTime;
    private String complianceStatus;

    private String symbol_color;
    private String device_id;
    private String fail_process_description;
    private int complianceValue;

    @Transient
    private Date createdOn;


    @Override
    public String toString() {
        return "CheckImeiRequest{" + "id=" + id + ", createdOn=" + createdOn + ", imei=" + imei + ", msisdn=" + msisdn + ", operator=" + operator + ", imsi=" + imsi + ", language=" + language + ", channel=" + channel + ", requestProcessStatus=" + requestProcessStatus + ", imeiProcessStatus=" + imeiProcessStatus + ", checkProcessTime=" + checkProcessTime + ", complianceStatus=" + complianceStatus + ", complianceValue=" + complianceValue + ", utm_source=" + utm_source + ", browser=" + browser + ", public_ip=" + public_ip + ", header_browser=" + header_browser + ", header_public_ip=" + header_public_ip + ", symbol_color=" + symbol_color + ", device_id=" + device_id + ", os_type=" + os_type + ", fail_process_description=" + fail_process_description + '}';
    }

}
