package com.gl.ceir.config.model.constants;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter //Lombok annotation
@Setter
@Builder
@Entity
public class CustomCheckImeiRequest {

    String imei;
    String serialNumber;
}
