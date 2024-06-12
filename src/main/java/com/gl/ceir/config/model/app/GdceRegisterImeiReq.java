package com.gl.ceir.config.model.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


//@NotNull
//@Size(min = 6, max = 20)
//@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).*$", message = "Password must contain at least one digit, one letter, and one special character")
@Setter
@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GdceRegisterImeiReq {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private String remark;
    private String requestId;
    private int imeiCount;
    private int successCount;
    private int failCount;
    private String fileName;

    public GdceRegisterImeiReq(String status, String remark, String requestId, int imeiCount, String fileName) {
        this.status = status;
        this.remark = remark;
        this.requestId = requestId;
        this.imeiCount = imeiCount;
        this.fileName = fileName;
    }


}
