package com.gl.ceir.config.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;


@Setter
@Getter
@Entity
@DynamicInsert
public class GdceCheckImeiReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("")
    private String imei;
    @ColumnDefault("")
    private String serialNumber;
    private String status;
    private String remark;
    private String requestId;
    private int imeiCount;

    public GdceCheckImeiReq() {
    }

    public GdceCheckImeiReq(String status, String remark, String requestId, int imeiCount) {
        this.status = status;
        this.remark = remark;
        this.requestId = requestId;
        this.imeiCount = imeiCount;
    }

    public GdceCheckImeiReq(String status, String remark) {
        this.status = status;
        this.remark = remark;
    }

    public GdceCheckImeiReq(Long id, String imei, String serialNumber, String status, String remark, String requestId, int imeiCount) {
        this.id = id;
        this.imei = imei;
        this.serialNumber = serialNumber;
        this.status = status;
        this.remark = remark;
        this.requestId = requestId;
        this.imeiCount = imeiCount;
    }

    @Override
    public String toString() {
        return "GdceCheckImeiReq{" +
                "id=" + id +
                ", imei='" + imei + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                ", requestId='" + requestId + '\'' +
                ", imeiCount=" + imeiCount +
                '}';
    }
}
